/* Copyright 2016 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For additional information, contact:
 * Environmental Systems Research Institute, Inc.
 * Attn: Contracts Dept
 * 380 New York Street
 * Redlands, California, USA 92373
 *
 * email: contracts@esri.com
 *
 */
package com.esri.android.nearbyplaces.map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.esri.android.nearbyplaces.PlaceListener;
import com.esri.android.nearbyplaces.R;
import com.esri.android.nearbyplaces.data.CategoryHelper;
import com.esri.android.nearbyplaces.data.Place;
import com.esri.android.nearbyplaces.filter.FilterDialogFragment;
import com.esri.android.nearbyplaces.filter.FilterPresenter;
import com.esri.android.nearbyplaces.places.PlacesActivity;
import com.esri.android.nearbyplaces.route.RouteDirectionsFragment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.*;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.tasks.route.DirectionManeuver;
import com.esri.arcgisruntime.tasks.route.Route;
import com.esri.arcgisruntime.tasks.route.RouteResult;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapFragment extends Fragment implements  MapContract.View, PlaceListener {

  private MapContract.Presenter mPresenter;

  private CoordinatorLayout mMapLayout;

  private MapView mMapView;

  private LocationDisplay mLocationDisplay;

  private GraphicsOverlay mGraphicOverlay, mRouteOverlay;


  private boolean initialLocationLoaded =false;

  private boolean mCenteringOnPlace = false;

  private Graphic mCenteredGraphic = null;

  private Place mCenteredPlace = null;

  private NavigationCompletedListener mNavigationCompletedListener;

  private final static String TAG = MapFragment.class.getSimpleName();

  private long mStartTime;

  private String centeredPlaceName;


  private BottomSheetBehavior bottomSheetBehavior;

  private FrameLayout mBottomSheet;

  private boolean mShowSnackbar = false;

  private boolean mRoutingState = false;

  private List<DirectionManeuver> mRouteDirections;

  private Viewpoint mViewpoint = null;

  public MapFragment(){}

  public static MapFragment newInstance(){
    return new MapFragment();
  }

  @Override
  public void onCreate(@NonNull Bundle savedInstance){

    super.onCreate(savedInstance);
    // retain this fragment
    setRetainInstance(true);

    // Set up the toolbar
    setUpToolbar();

    // Set toolbar to transparent on start of activity
    setToolbarTransparent();

    setHasOptionsMenu(true);/// allows invalidateOptionsMenu to work
    mMapLayout = (CoordinatorLayout) getActivity().findViewById(R.id.map_coordinator_layout);

    //Set up behavior for the bottom sheet
    setUpBottomSheet();
  }

  @Override
  @Nullable
  public View onCreateView(LayoutInflater layoutInflater, ViewGroup container,
      Bundle savedInstance){
    mStartTime = Calendar.getInstance().getTimeInMillis();
    View root = layoutInflater.inflate(R.layout.map_fragment, container,false);

    Intent intent = getActivity().getIntent();
    // If any extra data was sent, store it.
    if (intent.getSerializableExtra("PLACE_DETAIL") != null){
      centeredPlaceName = getActivity().getIntent().getStringExtra("PLACE_DETAIL");
    }
    if (intent.hasExtra("MIN_X")){

      double minX = intent.getDoubleExtra("MIN_X", 0);
      double minY = intent.getDoubleExtra("MIN_Y", 0);
      double maxX = intent.getDoubleExtra("MAX_X", 0);
      double maxY = intent.getDoubleExtra("MAX_Y", 0);
      String spatRefStr = intent.getStringExtra("SR");
      if (spatRefStr != null ){
        Envelope envelope = new Envelope(minX, minY, maxX, maxY, SpatialReference.create(spatRefStr));
        mViewpoint = new Viewpoint(envelope);
      }

      setUpMapView(root);
    }

    return root;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    // Inflate the menu items for use in the action bar
    inflater.inflate(R.menu.map_menu, menu);
  }

  /**
   * When activity first starts, the action bar
   * shows the back arrow for navigating back
   * to parent activity.  Menu item click listener
   * responds in one of the following ways:
   * 1) navigating back to list of places,
   * 2) showing the filter UI
   * 3) showing the route to selected place in map
   */
  private void setUpToolbar(){
    Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.map_toolbar);
    ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
    toolbar.setTitle("");
    final ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
    ab.setDisplayHomeAsUpEnabled(true);

    ab.setHomeAsUpIndicator(0); // Use default home icon
    // Menu items change depending on presence/absence of bottom sheet
    toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
      @Override public boolean onMenuItemClick(MenuItem item) {
        if (item.getTitle().toString().equalsIgnoreCase(getString(R.string.list_view))){
          // Show the list of places
          showList();
        }
        if (item.getTitle().toString().equalsIgnoreCase(getString(R.string.filter))){
          FilterDialogFragment dialogFragment = new FilterDialogFragment();
          FilterPresenter filterPresenter = new FilterPresenter();
          dialogFragment.setPresenter(filterPresenter);
          dialogFragment.show(getActivity().getFragmentManager(),"dialog_fragment");

        }
          if (item.getTitle().toString().equalsIgnoreCase("Route")){
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            final View routeHeaderView = inflater.inflate(R.layout.route_header,null);
            TextView tv = (TextView) routeHeaderView.findViewById(R.id.route_bar_title);
            tv.setElevation(6f);
            tv.setText(mCenteredPlace.getName());
            tv.setTextColor(Color.WHITE);
            ImageView btnClose = (ImageView) routeHeaderView.findViewById(R.id.btnClose);
            ImageView btnDirections = (ImageView) routeHeaderView.findViewById(R.id.btnDirections);
            final ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
            ab.hide();
            mMapView.addView(routeHeaderView, layout);
            btnClose.setOnClickListener(new View.OnClickListener() {
              @Override public void onClick(View v) {
                mMapView.removeView(routeHeaderView);
                ab.show();
                // Clear route
                if (mRouteOverlay != null){
                  mRouteOverlay.getGraphics().clear();
                }
                if (mViewpoint != null){
                  mMapView.setViewpoint(mViewpoint);
                }
                mPresenter.start();
              }
            });
            btnDirections.setOnClickListener(new View.OnClickListener() {
              @Override public void onClick(View v) {
                // show directions fragment
                RouteDirectionsFragment routeDirectionsFragment = new RouteDirectionsFragment();
                routeDirectionsFragment.show(getActivity().getFragmentManager(),"route_directions_fragment");
                routeDirectionsFragment.setRoutingDirections(mRouteDirections);
              }
            });
          mPresenter.getRoute();

        }
        return false;
      }
    });
  }

  /**
   * Switch to the list view of the places
   */
  private void showList(){
    Intent intent = new Intent(getActivity(), PlacesActivity.class);
    startActivity(intent);
  }

  /**
   * Sets the outline of the toolbar shadow to the background color
   * of the layout (transparent, in this case)
   */
  private void setToolbarTransparent(){
    AppBarLayout appBarLayout = (AppBarLayout) getActivity().findViewById(R.id.map_appbar);
    appBarLayout.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
  }

  /**
   * Add the map to the view and set up location display.
   * Once the map is drawn, kick off business logic.
   * @param root View
   */
  private void setUpMapView(View root){
    mMapView = (MapView) root.findViewById(R.id.map);

    Basemap basemap = Basemap.createStreets();

    ArcGISMap map = new ArcGISMap(basemap);
    mMapView.setMap(map);
    //If a Viewpoint is set immediately after calling setMap,
    // the Viewpoint will be cached, and then applied as soon
    // as the ArcGISMap is loaded, overriding the default Viewpoint.
    if (mViewpoint != null){
      mMapView.setViewpoint(mViewpoint);
    }

    // Add graphics overlay for map markers
    mGraphicOverlay  = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(mGraphicOverlay);

    mLocationDisplay = mMapView.getLocationDisplay();

    mLocationDisplay.startAsync();

    mMapView.addDrawStatusChangedListener(new DrawStatusChangedListener() {
      @Override public void drawStatusChanged(DrawStatusChangedEvent drawStatusChangedEvent) {
        if (drawStatusChangedEvent.getDrawStatus() == DrawStatus.COMPLETED){
          long elapsedTime = (Calendar.getInstance().getTimeInMillis() - mStartTime);
          Log.i("MapFragment", "Time to DrawStatus.COMPLETED = " + Long.toString(elapsedTime) + " ms");
          mPresenter.start();
          mMapView.removeDrawStatusChangedListener(this);
        }
      }
    });

    // Setup OnTouchListener to detect and act on long-press
    mMapView.setOnTouchListener(new MapTouchListener(getActivity().getApplicationContext(), mMapView));
  }

  /**
   * Attach display logic to bottom sheet behavior.
   */
  private void setUpBottomSheet(){
    bottomSheetBehavior = BottomSheetBehavior.from(getActivity().findViewById(R.id.bottom_card_view));

    bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
      @Override
      public void onStateChanged(View bottomSheet, int newState) {
        getActivity().invalidateOptionsMenu();
        if (newState == BottomSheetBehavior.STATE_COLLAPSED && mShowSnackbar) {
          clearCenteredPin();
          showSearchSnackbar();
          mShowSnackbar = false;
        }
      }

      @Override
      public void onSlide(View bottomSheet, float slideOffset) {
      }
    });

    mBottomSheet = (FrameLayout) getActivity().findViewById(R.id.bottom_card_view);
  }


  /**
   * Set the menu options based on
   * the bottom sheet state
   *
   */
  @Override
  public void onPrepareOptionsMenu(Menu menu){
    MenuItem listItem = menu.findItem(R.id.list_action);
    MenuItem routeItem = menu.findItem(R.id.route_action);
    MenuItem directionItem = menu.findItem(R.id.walking_directions);
    MenuItem filterItem = menu.findItem(R.id.filter_in_map);

    if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED  && !mRoutingState){
      listItem.setVisible(true);
      filterItem.setVisible(true);
      routeItem.setVisible(false);
    }else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED && !mRoutingState){
      listItem.setVisible(false);
      filterItem.setVisible(true);
      routeItem.setVisible(true);
    }
    if (mRoutingState){
      listItem.setVisible(false);
      filterItem.setVisible(false);
      routeItem.setVisible(false);
      directionItem.setVisible(true);
    }else{
      directionItem.setVisible(false);
    }
  }

  /**
   * Show snackbar prompting user about
   * scanning for new locations
   */
  private void showSearchSnackbar(){
    Snackbar snackbar = Snackbar
        .make(mMapLayout, "Search for places?", Snackbar.LENGTH_LONG)
        .setAction("SEARCH", new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            if (mRouteOverlay != null){
              mRouteOverlay.getGraphics().clear();
            }
            mCenteredPlace = null;
            mPresenter.findPlacesNearby();
          }
        });

    snackbar.show();
  }

  /**
   * Attach the viewpoint change listener
   * so that POIs get updated as map's
   * visible area is changed.
   */
  private void setNavigationCompletedListener(){
    mNavigationCompletedListener = new NavigationCompletedListener() {
      @Override public void navigationCompleted(NavigationCompletedEvent navigationCompletedEvent) {
          onMapScroll();
      }
    };
    mMapView.addNavigationCompletedListener(mNavigationCompletedListener);
  }


  private void removeNavigationCompletedListener(){
    if (mNavigationCompletedListener != null){
      mMapView.removeNavigationCompletedListener(mNavigationCompletedListener);
      mNavigationCompletedListener = null;
    }
  }

  @Override
  public void onResume(){
    super.onResume();
    mMapView.resume();
   if (!mLocationDisplay.isStarted()){
      mLocationDisplay.startAsync();
    }
  }

  @Override
  public void onPause(){
    super.onPause();
    mMapView.pause();
   if (mLocationDisplay.isStarted()){
      mLocationDisplay.stop();
    }
  }

  /**
   * If any places are found,
   * add them to the map as graphics.
   * @param places List of Place items
   */
  @Override public void showNearbyPlaces(List<Place> places) {
    if (!initialLocationLoaded){
      setNavigationCompletedListener();
    }
    initialLocationLoaded = true;
    if (places.isEmpty()){
      Toast.makeText(getContext(),getString(R.string.no_places_found),Toast.LENGTH_SHORT).show();
      return;
    }
    // Clear out any existing graphics
    mGraphicOverlay.getGraphics().clear();

    // Create a graphic for every place
    for (Place place : places){
      BitmapDrawable pin = (BitmapDrawable) ContextCompat.getDrawable(getActivity(),getDrawableForPlace(place)) ;
      final PictureMarkerSymbol pinSymbol = new PictureMarkerSymbol(pin);
      Point graphicPoint = place.getLocation();
      Graphic graphic = new Graphic(graphicPoint, pinSymbol);
      mGraphicOverlay.getGraphics().add(graphic);
    }

    // If a centered place name is not null,
    // show detail view
    if (centeredPlaceName != null){
      for (Place p: places){
        if (p.getName().equalsIgnoreCase(centeredPlaceName)){
          showDetail(p);
          centeredPlaceName = null;
          break;
        }
      }
    }
  }
  /**
   * Populate the place detail contained
   * within the bottom sheet
   * @param place - Place item selected by user
   */
  @Override public void showDetail(Place place) {
    TextView txtName = (TextView) mBottomSheet.findViewById(R.id.placeName);
    txtName.setText(place.getName());
    String address = place.getAddress();
    String[] splitStrs = TextUtils.split(address, ",");
    if (splitStrs.length>0)                                   {
      address = splitStrs[0];
    }
    TextView txtAddress = (TextView) mBottomSheet.findViewById(R.id.placeAddress) ;
    txtAddress.setText(address);
    TextView txtPhone  = (TextView) mBottomSheet.findViewById(R.id.placePhone) ;
    txtPhone.setText(place.getPhone());
    TextView txtUrl = (TextView) mBottomSheet.findViewById(R.id.placeUrl);
    txtUrl.setText(place.getURL());
    TextView txtType = (TextView) mBottomSheet.findViewById(R.id.placeType) ;
    txtType.setText(place.getType());

    // Assign the appropriate icon
    Drawable d =   CategoryHelper.getDrawableForPlace(place, getActivity()) ;
    ImageView icon = (ImageView) getActivity().findViewById(R.id.TypeIcon);
    icon.setImageDrawable(d);
    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    // Center map on selected place
    mPresenter.centerOnPlace(place);
    mShowSnackbar = false;
    centeredPlaceName = place.getName();
  }

  /**
   * Dismiss the bottom sheet
   * when map is scrolled.
   */
  @Override public void onMapScroll() {
    mShowSnackbar = true;
    if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){ // show snackbar prompting for re-doing search
      showSearchSnackbar();
    }else{
      bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
  }

  /**
   * Assign appropriate drawable given place
   * @param p - the place item to be displayed
   * @return - the appropriate drawable for the given place
   */
  private int getDrawableForPlace(Place p){
    int d = CategoryHelper.getPinForPlace(p);
    return d;
  }
  private int getPinForCenterPlace(Place p){
    return CategoryHelper.getPinForCenterPlace(p);
  }

  @Override public MapView getMapView() {
    return mMapView;
  }


  /**
   * Center the selected place and change the pin
   * color to blue.
   * @param p - the selected place
   */
  @Override public void centerOnPlace(Place p) {
    // Keep track of centered place since
    // it will be needed to reset
    // the graphic if another place
    // is centered.
    mCenteredPlace = p;

    // Stop listening to navigation changes
    // while place is centered in map.
    removeNavigationCompletedListener();
    mCenteringOnPlace = true;
    ListenableFuture<Boolean>  viewCentered = mMapView.setViewpointCenterAsync(p.getLocation());
    viewCentered.addDoneListener(new Runnable() {
      @Override public void run() {
        // Once we've centered on a place, listen
        // for changes in viewpoint.
        if (mNavigationCompletedListener == null){
          setNavigationCompletedListener();
        }
      }
    });
    // Change the pin icon
    clearCenteredPin();

    List<Graphic> graphics = mGraphicOverlay.getGraphics();
    for (Graphic g : graphics){
      if (g.getGeometry().equals(p.getLocation())){
        mCenteredGraphic = g;
        mCenteredGraphic.setZIndex(3);
        BitmapDrawable pin = (BitmapDrawable) ContextCompat.getDrawable(getActivity(),getPinForCenterPlace(p)) ;
        final PictureMarkerSymbol pinSymbol = new PictureMarkerSymbol(pin);
        g.setSymbol(pinSymbol);
        break;
      }
    }
  }

  /**
   * Restore a pin to its default color
   */
  private void clearCenteredPin(){
    if (mCenteredGraphic != null){
      mCenteredGraphic.setZIndex(0);
      BitmapDrawable oldPin = (BitmapDrawable) ContextCompat.getDrawable(getActivity(),getDrawableForPlace(mCenteredPlace)) ;
      mCenteredGraphic.setSymbol(new PictureMarkerSymbol(oldPin));
    }
  }

  /**
   * Clear the graphics overlay
   */
  private void clearPlaceGraphicOverlay(){
      mGraphicOverlay.getGraphics().clear();
  }

  /**
   * Show the returned route on the map
   * @param routeResult - RouteResult returned from the routing task
   * @param beginPoint - the point representing the start of the route
   * @param endPoint - the point representing the end of the route
   */
  @Override public void showRoute(RouteResult routeResult, Point beginPoint, Point endPoint) {
    Route route;
    try {
      route = routeResult.getRoutes().get(0);
      if (route.getTotalLength() == 0.0) {
        throw new Exception("Can not find the Route");
      }
    } catch (Exception e) {
      Toast.makeText(getActivity(),
          "We are sorry, we couldn't find the route. Please make "
              + "sure the Source and Destination are different or are connected by road",
          Toast.LENGTH_LONG).show();
      Log.e(TAG, e.getMessage());
      return;
    }

    // Clear all place graphics
    clearPlaceGraphicOverlay();

    // Clear any previous route
    if (mRouteOverlay == null) {
      mRouteOverlay = new GraphicsOverlay();
    }else{

      mRouteOverlay.getGraphics().clear();
    }
    // Create polyline graphic of the full route
    SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 6);
    Graphic routeGraphic = new Graphic(route.getRouteGeometry(), lineSymbol);

    // Add the route graphic to the route layer
    mRouteOverlay.getGraphics().add(routeGraphic);
    // Add start and end pins
    BitmapDrawable startPin = (BitmapDrawable) ContextCompat.getDrawable(getActivity(),R.drawable.route_pin_start) ;
    BitmapDrawable endPin = (BitmapDrawable) ContextCompat.getDrawable(getActivity(),R.drawable.end_route_pin) ;
    // Current location from Google location services
    // needs a spatial reference before it can be added to map.
    Point startPoint = new Point(beginPoint.getX(), beginPoint.getY(), endPoint.getSpatialReference());

    Graphic begin = generateRoutePoints(startPoint, startPin);
    mRouteOverlay.getGraphics().add(begin);
    mRouteOverlay.getGraphics().add(generateRoutePoints(endPoint,endPin));
    mMapView.getGraphicsOverlays().add(mRouteOverlay);


    // Zoom to the extent of the entire route with a padding
    Geometry shape = routeGraphic.getGeometry();
    mMapView.setViewpointGeometryWithPaddingAsync(shape, 400);

    // Get routing directions
    mRouteDirections = route.getDirectionManeuvers();
  }
  /**
   * Converts device specific pixels to density independent pixels.
   *
   * @param context - Activity Context
   * @param px - number of device specific pixels
   * @return number of density independent pixels
   */
  private float convertPixelsToDp(Context context, float px) {
    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    return px / (metrics.densityDpi / 160f);
  }

  /**
   * Helper method for creating start/end graphic used
   * to display the route on the map
   * @param p - point representing location on the map
   * @param pin - BitmapDrawable to use for the returned graphic
   * @return - a graphic representing the point
   */
  private Graphic generateRoutePoints(Point p, BitmapDrawable pin){
    float offsetY = convertPixelsToDp(getActivity(), pin.getBounds().bottom);
    PictureMarkerSymbol symbol = new PictureMarkerSymbol(pin);
    symbol.setOffsetY(offsetY);
    return new Graphic(p, symbol);
  }
  /**
   *
   * @param presenter -  the MapContract.Presenter encapsulating
   * the business logic for the map.
   */
  @Override public void setPresenter(MapContract.Presenter presenter) {
    mPresenter = presenter;
  }

  /**
   * Given a map point, find the associated Place
   * @param p - a point representing a geolocation
   * @return - the place found at that geolocation
   */
  private Place getPlaceForPoint(Point p){
    Place foundPlace = mPresenter.findPlaceForPoint(p);
    return foundPlace;
  }
  private class MapTouchListener extends DefaultMapViewOnTouchListener {
    /**
     * Instantiates a new DrawingMapViewOnTouchListener with the specified
     * context and MapView.
     *
     * @param context the application context from which to get the display
     *                metrics
     * @param mapView the MapView on which to control touch events
     */
    public MapTouchListener(Context context, MapView mapView) {
      super(context, mapView);
    }
    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
      removeNavigationCompletedListener();
      android.graphics.Point screenPoint = new android.graphics.Point(
          (int) motionEvent.getX(),
          (int) motionEvent.getY());
      // identify graphics on the graphics overlay
      final ListenableFuture<List<Graphic>> identifyGraphic = mMapView
          .identifyGraphicsOverlayAsync(mGraphicOverlay, screenPoint, 10, 2);

      identifyGraphic.addDoneListener(new Runnable() {
        @Override
        public void run() {
          try {
            // get the list of graphics returned by identify
            List<Graphic> graphic = identifyGraphic.get();

            // get size of list in results
            int identifyResultSize = graphic.size();
            if (identifyResultSize > 0){
              Graphic foundGraphic = graphic.get(0);
              Place foundPlace = getPlaceForPoint((Point)foundGraphic.getGeometry());
              if (foundPlace != null){
                showDetail(foundPlace);
              }
            }
          } catch (InterruptedException | ExecutionException ie) {
            ie.printStackTrace();
          }
        }

      });
      return true;
    }
  }

}
