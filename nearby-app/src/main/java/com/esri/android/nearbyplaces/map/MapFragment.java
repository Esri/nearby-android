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

import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
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
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.esri.android.nearbyplaces.PlaceListener;
import com.esri.android.nearbyplaces.R;
import com.esri.android.nearbyplaces.data.CategoryHelper;
import com.esri.android.nearbyplaces.data.LocationService;
import com.esri.android.nearbyplaces.data.Place;
import com.esri.android.nearbyplaces.data.PlacesServiceApi;
import com.esri.android.nearbyplaces.filter.FilterContract;
import com.esri.android.nearbyplaces.filter.FilterDialogFragment;
import com.esri.android.nearbyplaces.filter.FilterPresenter;
import com.esri.android.nearbyplaces.places.PlacesActivity;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.*;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.DrawStatus;
import com.esri.arcgisruntime.mapping.view.DrawStatusChangedEvent;
import com.esri.arcgisruntime.mapping.view.DrawStatusChangedListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.NavigationChangedEvent;
import com.esri.arcgisruntime.mapping.view.NavigationChangedListener;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.tasks.networkanalysis.DirectionManeuver;
import com.esri.arcgisruntime.tasks.networkanalysis.Route;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult;

public class MapFragment extends Fragment implements  MapContract.View, PlaceListener {

  private MapContract.Presenter mPresenter = null;

  private CoordinatorLayout mMapLayout = null;

  private MapView mMapView = null;

  private LocationDisplay mLocationDisplay = null;

  private GraphicsOverlay mGraphicOverlay = null;

  private GraphicsOverlay mRouteOverlay = null;


  private boolean initialLocationLoaded = false;

  private Graphic mCenteredGraphic = null;

  @Nullable private Place mCenteredPlace = null;

  @Nullable private NavigationChangedListener mNavigationChangedListener = null;

  private final static String TAG = MapFragment.class.getSimpleName();

  private int mCurrentPosition = 0;

  @Nullable private String centeredPlaceName = null;


  private LinearLayout mRouteHeaderDetail = null;

  private LinearLayout mSegmentNavigator = null;

  private BottomSheetBehavior bottomSheetBehavior = null;

  private FrameLayout mBottomSheet = null;

  private boolean mShowingRouteDetail = false;

  private boolean mShowSnackbar = false;

  private List<DirectionManeuver> mRouteDirections = null;

  private Viewpoint mViewpoint = null;

  private View mRouteHeaderView = null;

  private ProgressDialog mProgressDialog = null;

  private RouteResult mRouteResult = null;

  private Point mStart = null;

  private Point mEnd = null;

  public MapFragment(){}

  public static MapFragment newInstance(){
    return new MapFragment();
  }

  @Override
  public final void onCreate( final Bundle savedInstance){

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
  public final View onCreateView(final LayoutInflater layoutInflater, final ViewGroup container,
      final Bundle savedInstance){
    final View root = layoutInflater.inflate(R.layout.map_fragment, container,false);

    final Intent intent = getActivity().getIntent();
    // If any extra data was sent, store it.
    if (intent.getSerializableExtra("PLACE_DETAIL") != null){
      centeredPlaceName = getActivity().getIntent().getStringExtra("PLACE_DETAIL");
    }
    if (intent.hasExtra("MIN_X")){

      final double minX = intent.getDoubleExtra("MIN_X", 0);
      final double minY = intent.getDoubleExtra("MIN_Y", 0);
      final double maxX = intent.getDoubleExtra("MAX_X", 0);
      final double maxY = intent.getDoubleExtra("MAX_Y", 0);
      final String spatRefStr = intent.getStringExtra("SR");
      if (spatRefStr != null ){
        final Envelope envelope = new Envelope(minX, minY, maxX, maxY, SpatialReference.create(spatRefStr));
        mViewpoint = new Viewpoint(envelope);
      }
    }
    showProgressIndicator("Loading map");
    setUpMapView(root);
    return root;
  }

  @Override
  public final void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
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
    final Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.map_toolbar);
    ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
    toolbar.setTitle("");
    final ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
    if (ab != null){
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setHomeAsUpIndicator(0); // Use default home icon
    }

    // Menu items change depending on presence/absence of bottom sheet
    toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
      @Override public boolean onMenuItemClick(final MenuItem item) {
        if (item.getTitle().toString().equalsIgnoreCase(getString(R.string.list_view))){
          // Show the list of places
          showList();
        }
        if (item.getTitle().toString().equalsIgnoreCase(getString(R.string.filter))){
          final FilterDialogFragment dialogFragment = new FilterDialogFragment();
          final FilterContract.Presenter filterPresenter = new FilterPresenter();
          dialogFragment.setPresenter(filterPresenter);
          dialogFragment.show(getActivity().getFragmentManager(),"dialog_fragment");

        }
          if (item.getTitle().toString().equalsIgnoreCase("Route")){
            mPresenter.getRoute();

        }
        return false;
      }
    });
  }

  @Override public void showMessage(final String message) {
    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
  }

  @Override public void showProgressIndicator(final String message) {
    if (mProgressDialog == null){
      mProgressDialog = new ProgressDialog(getActivity());
    }
    mProgressDialog.dismiss();
    mProgressDialog.setTitle(getString(R.string.nearby_places));
    mProgressDialog.setMessage(message);
    mProgressDialog.show();
  }

  @Override public void showRouteDetail(final int position) {
    // Remove the route header view,
    //
    //since we're replacing it with a different header

    removeRouteHeaderView();
    // State  and stage flags
    mCurrentPosition = position;
    mShowingRouteDetail = true;

    // Hide action bar
    final ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
    if (ab != null){
      ab.hide();
    }

    // Display route detail header
   final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    final LinearLayout.LayoutParams routeDetailLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT);

    if (mRouteHeaderDetail == null){
      mRouteHeaderDetail = (LinearLayout) inflater.inflate(R.layout.route_detail_header, null);
      TextView title = (TextView) mRouteHeaderDetail.findViewById(R.id.route_txt_detail) ;
      title.setText("Route Detail");

      mRouteHeaderDetail.setBackgroundColor(Color.WHITE);
      mMapView.addView(mRouteHeaderDetail, routeDetailLayout);
      mMapView.requestLayout();

      // Attach a listener to the back arrow
      ImageView imageBtn = (ImageView) mRouteHeaderDetail.findViewById(R.id.btnDetailHeaderClose) ;
      imageBtn.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          // Navigate back to directions list
          mShowingRouteDetail = false;
          ((MapActivity)getActivity()).showDirections(mRouteDirections);
        }
      });
    }



    // Display arrows to scroll through directions
    if (mSegmentNavigator == null){
      mSegmentNavigator = (LinearLayout)inflater.inflate(R.layout.navigation_arrows,null);
      final FrameLayout navigatorLayout = (FrameLayout) getActivity().findViewById(R.id.map_fragment_container);
      FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
          ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM|Gravity.END);
      frameLayoutParams.setMargins(0,0,0,80);
      navigatorLayout.addView(mSegmentNavigator, frameLayoutParams);
      navigatorLayout.requestLayout();
      // Add button click listeners
      Button btnPrev = (Button) getActivity().findViewById(R.id.btnBack) ;

      btnPrev.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          if (mCurrentPosition > 0){
            populateViewWithRouteDetail(mRouteDirections.get(mCurrentPosition - 1));
            mCurrentPosition = mCurrentPosition - 1;
          }

        }
      });
      Button btnNext = (Button) getActivity().findViewById(R.id.btnNext);
      btnNext.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          if (mCurrentPosition < mRouteDirections.size() -1){
            populateViewWithRouteDetail(mRouteDirections.get(mCurrentPosition + 1));
            mCurrentPosition = mCurrentPosition + 1;
          }

        }
      });
    }



    // Populate with directions
    DirectionManeuver maneuver = mRouteDirections.get(position);
    populateViewWithRouteDetail(maneuver);

  }

  @Override public void restoreMapView() {
    removeRouteHeaderView();
  }

  @Override public void getRoute(final LocationService service) {
    service.getRouteFromService(service.getCurrentLocation(), mCenteredPlace.getLocation(), getContext() ,
        new PlacesServiceApi.RouteServiceCallback() {
          @Override public void onRouteReturned(final RouteResult result) {
            setRoute(result, service.getCurrentLocation(),mCenteredPlace.getLocation());
          }
        });
  }

  /**
   * Remove special header and navigator buttons for route detail
   */
  public void removeRouteDetail(){
    mMapView.removeView(mRouteHeaderDetail);
    mRouteHeaderDetail = null;
    final FrameLayout navigatorLayout = (FrameLayout) getActivity().findViewById(R.id.map_fragment_container);
    navigatorLayout.removeView(mSegmentNavigator);
    mMapView.removeView(mSegmentNavigator);
    mSegmentNavigator = null;
  }

  /**
   * Show text directions for a specific route segment
   * @param maneuver - DirectionManeuver corresponding to the specific direction segment
   */
  private void populateViewWithRouteDetail(DirectionManeuver maneuver ){
    TextView direction = (TextView) mRouteHeaderDetail.findViewById(R.id.directions_text_textview);

    direction.setText(maneuver.getDirectionText());
    TextView travelDistance = (TextView) mRouteHeaderDetail.findViewById(R.id.directions_length_textview);
    travelDistance.setText(String.format("%.1f meters", maneuver.getLength()));

    // Remove any previously highlighted graphic
    // that we may have added
    if (mRouteOverlay.getGraphics().size() == 4){
      mRouteOverlay.getGraphics().remove(0);
    }

    // Highlight route segment in map
    Geometry geometry = maneuver.getGeometry();
    Graphic graphic = null;
    if (geometry instanceof Polyline){
      SimpleLineSymbol symbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 7);
      graphic = new Graphic(geometry, symbol);
    }else{
      SimpleMarkerSymbol marker = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 7);
      graphic = new Graphic(geometry, marker);
    }

    mRouteOverlay.getGraphics().add(0,graphic);

    // Zoom to segment
    mMapView.setViewpointGeometryAsync(geometry,70);
  }

  /**
   * Show a special header when routes are displayed
   */
  private void showRouteHeader(double travelTime){
    final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    final LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT);
    mRouteHeaderView = inflater.inflate(R.layout.route_header,null);
    final TextView tv = (TextView) mRouteHeaderView.findViewById(R.id.route_bar_title);
    tv.setElevation(6f);
    tv.setText(mCenteredPlace != null ? mCenteredPlace.getName() : null);
    tv.setTextColor(Color.WHITE);
    final TextView time = (TextView) mRouteHeaderView.findViewById(R.id.routeTime);
    time.setText(Math.round(travelTime)+" min");
    final ImageView btnClose = (ImageView) mRouteHeaderView.findViewById(R.id.btnClose);
    final ImageView btnDirections = (ImageView) mRouteHeaderView.findViewById(R.id.btnDirections);
    final ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
    if (ab != null){
      ab.hide();
    }

    mMapLayout.addView(mRouteHeaderView, layout);
    btnClose.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(final View v) {
        mMapLayout.removeView(mRouteHeaderView);
        if (ab != null){
          ab.show();
        }

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
      @Override public void onClick(final View v) {
        // show directions
        ((MapActivity)getActivity()).showDirections(mRouteDirections);
      }
    });
  }

  /**
   * Remove RouteHeader view from map view and restore action bar
   */
  private void removeRouteHeaderView(){
    if(mRouteHeaderView != null){
      mMapLayout.removeView(mRouteHeaderView);
    }
    final ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
    if (ab != null){
      ab.show();
    }
  }

  /**
   * Switch to the list view of the places
   */
  private void showList(){
    final Intent intent = new Intent(getActivity(), PlacesActivity.class);
    startActivity(intent);
  }

  /**
   * Sets the outline of the toolbar shadow to the background color
   * of the layout (transparent, in this case)
   */
  private void setToolbarTransparent(){
    final AppBarLayout appBarLayout = (AppBarLayout) getActivity().findViewById(R.id.map_appbar);
    appBarLayout.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
  }

  /**
   * Add the map to the view and set up location display.
   * Once the map is drawn, kick off business logic.
   * @param root View
   */
  private void setUpMapView(final View root){
    mMapView = (MapView) root.findViewById(R.id.map);
    final ArcGISMap map = new ArcGISMap(Basemap.createNavigationVector());
    mMapView.setMap(map);

    if (mViewpoint != null){
      mMapView.setViewpoint(mViewpoint);
    }

    // Add graphics overlay for map markers
    mGraphicOverlay  = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(mGraphicOverlay);

    mMapView.addDrawStatusChangedListener(new DrawStatusChangedListener() {
      @Override public void drawStatusChanged(final DrawStatusChangedEvent drawStatusChangedEvent) {
        if (drawStatusChangedEvent.getDrawStatus() == DrawStatus.COMPLETED){
          if (mProgressDialog != null){
            mProgressDialog.dismiss();
          }
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
    // Explicitly set the peek height (otherwise bottom sheet is shown when map initially loads)
    bottomSheetBehavior.setPeekHeight(0);


    bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
      @Override
      public void onStateChanged(@NonNull final View bottomSheet, final int newState) {
        getActivity().invalidateOptionsMenu();
        if ((newState == BottomSheetBehavior.STATE_COLLAPSED) && mShowSnackbar) {
          clearCenteredPin();
          showSearchSnackbar();
          mShowSnackbar = false;
        }
      }

      @Override
      public void onSlide(@NonNull final View bottomSheet, final float slideOffset) {
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
  public final void onPrepareOptionsMenu(final Menu menu){
    final MenuItem listItem = menu.findItem(R.id.list_action);
    final MenuItem routeItem = menu.findItem(R.id.route_action);
    final MenuItem directionItem = menu.findItem(R.id.walking_directions);
    final MenuItem filterItem = menu.findItem(R.id.filter_in_map);


    if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
      listItem.setVisible(true);
      filterItem.setVisible(true);
      routeItem.setVisible(false);
    }else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
      listItem.setVisible(false);
      filterItem.setVisible(true);
      routeItem.setVisible(true);
    }
  }

  /**
   * Show snackbar prompting user about
   * scanning for new locations
   */
  private void showSearchSnackbar(){
    final Snackbar snackbar = Snackbar
        .make(mMapLayout, "Search for places?", Snackbar.LENGTH_LONG)
        .setAction("SEARCH", new View.OnClickListener() {
          @Override
          public void onClick(final View view) {
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
   * Attach the navigation change listener
   * so that points of interest get updated
   * as the map's visible area is changed.
   */
  private void setNavigationChangeListener(){
    mNavigationChangedListener = new NavigationChangedListener() {
      // This is a workaround for detecting when a fling
      // motion has completed on the map view. The
      // NavigationChangedListener listens for navigation changes,
      // not whether navigation has completed.  We wait
      // a small interval before checking if
      // map is view still navigating.
      @Override public void navigationChanged(final NavigationChangedEvent navigationChangedEvent) {
       if (!mMapView.isNavigating()){
         Handler handler = new Handler();
         handler.postDelayed(new Runnable() {

           @Override public void run() {

             if (!mMapView.isNavigating()) {
               onMapViewChange();
             }
           }
         }, 50);
       }
      }

    };
    mMapView.addNavigationChangedListener(mNavigationChangedListener);
  }

  /**
   * Remove navigation change listener
   */
  private void removeNavigationChangedListener(){
    if (mNavigationChangedListener != null){
      mMapView.removeNavigationChangedListener(mNavigationChangedListener);
      mNavigationChangedListener = null;
    }
  }

  @Override
  public final void onResume(){
    super.onResume();
    if (mMapView != null){
      mMapView.resume();
      // Turn on location display
      mLocationDisplay = mMapView.getLocationDisplay();
      if (mLocationDisplay != null && !mLocationDisplay.isStarted()){
        mLocationDisplay.startAsync();
      }
    }
  }

  @Override
  public final void onPause(){
    super.onPause();
    mMapView.pause();
    if (mLocationDisplay != null && mLocationDisplay.isStarted()){
      mLocationDisplay.stop();
    }
  }

  /**
   * If any places are found,
   * add them to the map as graphics.
   * @param places List of Place items
   */
  @Override public final void showNearbyPlaces(final List<Place> places) {

    // Clear out any existing graphics
    mGraphicOverlay.getGraphics().clear();

    if (!initialLocationLoaded){
      setNavigationChangeListener();
    }
    initialLocationLoaded = true;
    if (places == null || places.isEmpty()){
      Toast.makeText(getContext(),getString(R.string.no_places_found),Toast.LENGTH_SHORT).show();
      if (mProgressDialog !=  null){
        mProgressDialog.dismiss();
      }
      return;
    }

    // Create a graphic for every place
    for (final Place place : places){
      final BitmapDrawable pin = (BitmapDrawable) ContextCompat.getDrawable(getActivity(),getDrawableForPlace(place)) ;
      addGraphicToMap(pin, place.getLocation());
    }

    // If a centered place name is not null,
    // show detail view
    if (centeredPlaceName != null){
      for (final Place p: places){
        if (p.getName().equalsIgnoreCase(centeredPlaceName)){
          showDetail(p);
          centeredPlaceName = null;
          break;
        }
      }
    }
    if (mProgressDialog !=  null){
      mProgressDialog.dismiss();
    }

  }
  private void addGraphicToMap(BitmapDrawable bitdrawable, Geometry geometry){
    final PictureMarkerSymbol pinSymbol = new PictureMarkerSymbol(bitdrawable);
    final Graphic graphic = new Graphic(geometry, pinSymbol);
    mGraphicOverlay.getGraphics().add(graphic);
  }
  /**
   * Populate the place detail contained
   * within the bottom sheet
   * @param place - Place item selected by user
   */
  @Override public final void showDetail(final Place place) {
    final TextView txtName = (TextView) mBottomSheet.findViewById(R.id.placeName);
    txtName.setText(place.getName());
    String address = place.getAddress();
    final String[] splitStrs = TextUtils.split(address, ",");
    if (splitStrs.length>0)                                   {
      address = splitStrs[0];
    }
    final TextView txtAddress = (TextView) mBottomSheet.findViewById(R.id.placeAddress) ;
    txtAddress.setText(address);
    final TextView txtPhone  = (TextView) mBottomSheet.findViewById(R.id.placePhone) ;
    txtPhone.setText(place.getPhone());

    final LinearLayout linkLayout = (LinearLayout) mBottomSheet.findViewById(R.id.linkLayout);
    // Hide the link placeholder if no link is found
    if (place.getURL().isEmpty()) {
      linkLayout.setLayoutParams(new LinearLayoutCompat.LayoutParams(0, 0));
      linkLayout.requestLayout();
    }else {
      final int height = (int) (48 * Resources.getSystem().getDisplayMetrics().density);
      linkLayout.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
          height));
      linkLayout.requestLayout();
      final TextView txtUrl = (TextView) mBottomSheet.findViewById(R.id.placeUrl);
      txtUrl.setText(place.getURL());
    }


    final TextView txtType = (TextView) mBottomSheet.findViewById(R.id.placeType) ;
    txtType.setText(place.getType());

    // Assign the appropriate icon
    final Drawable d =   CategoryHelper.getDrawableForPlace(place, getActivity()) ;
    txtType.setCompoundDrawablesWithIntrinsicBounds(d,null,null,null);
    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    // Center map on selected place
    mPresenter.centerOnPlace(place);
    mShowSnackbar = false;
    centeredPlaceName = place.getName();
  }

  /**
   * Dismiss the bottom sheet
   * when map is scrolled and notify
   * presenter
   */
  @Override public final void onMapViewChange() {
    mShowSnackbar = true;
    if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
      if (!mShowingRouteDetail){
        // show snackbar prompting for re-doing search
        showSearchSnackbar();
      }

    }else{
      bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
    mPresenter.setCurrentExtent(mMapView.getVisibleArea().getExtent());
  }

  /**
   * Assign appropriate drawable given place
   * @param p - the place item to be displayed
   * @return - the appropriate id representing the drawable for the given place
   */
  private static int getDrawableForPlace(final Place p){
    return CategoryHelper.getResourceIdForPlacePin(p);
  }
  private static int getPinForCenterPlace(final Place p){
    return CategoryHelper.getPinForCenterPlace(p);
  }

  @Override public final MapView getMapView() {
    return mMapView;
  }


  /**
   * Center the selected place and change the pin
   * color to blue.
   * @param p - the selected place
   */
  @Override public final void centerOnPlace(final Place p) {
    if (p.getLocation() == null){
      return;
    }
    // Dismiss the route header view
    removeRouteHeaderView();

    // Keep track of centered place since
    // it will be needed to reset
    // the graphic if another place
    // is centered.
    mCenteredPlace = p;

    // Stop listening to navigation changes
    // while place is centered in map.
    removeNavigationChangedListener();
    final ListenableFuture<Boolean>  viewCentered = mMapView.setViewpointCenterAsync(p.getLocation());
    viewCentered.addDoneListener(new Runnable() {
      @Override public void run() {
        // Once we've centered on a place, listen
        // for changes in viewpoint.
        if (mNavigationChangedListener == null){
          setNavigationChangeListener();
        }
      }
    });
    // Change the pin icon
    clearCenteredPin();

    final List<Graphic> graphics = mGraphicOverlay.getGraphics();
    for (final Graphic g : graphics){
      if (g.getGeometry().equals(p.getLocation())){
        mCenteredGraphic = g;
        mCenteredGraphic.setZIndex(3);
        final BitmapDrawable pin = (BitmapDrawable) ContextCompat.getDrawable(getActivity(),getPinForCenterPlace(p)) ;
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
      final BitmapDrawable oldPin = (BitmapDrawable) ContextCompat.getDrawable(getActivity(),getDrawableForPlace(mCenteredPlace)) ;
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
   * Set the returned route details
   * @param routeResult - RouteResult returned from the routing task
   * @param beginPoint - the point representing the start of the route
   * @param endPoint - the point representing the end of the route
   */
  @Override public final void setRoute(final RouteResult routeResult, final Point beginPoint, final Point endPoint) {
    mRouteResult = routeResult;
    mStart = beginPoint;
    mEnd = endPoint;
    displayRoute();
  }

  /**
   * Show the route
   */
  public final void displayRoute() {
    if ( mRouteResult != null && mStart != null && mEnd != null)  {
      final Route route;
      try {
        route = mRouteResult.getRoutes().get(0);
        if (route.getTotalLength() == 0.0) {
          throw new Exception("Can not find the Route");
        }
      } catch (final Exception e) {
        Toast.makeText(getActivity(),
            "We're sorry, we couldn't find the route. Please make "
                + "sure the Start and Destination are different or are connected by a road",
            Toast.LENGTH_LONG).show();
        Log.e(MapFragment.TAG, e.getMessage());
        return;
      }

      // Clear all place graphics
      clearPlaceGraphicOverlay();


      if (mRouteOverlay == null) {
        mRouteOverlay = new GraphicsOverlay();
        mMapView.getGraphicsOverlays().add(mRouteOverlay);
      }else{
        // Clear any previous route
        mRouteOverlay.getGraphics().clear();
      }
      // Create polyline graphic of the full route
      final SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 6);
      final Graphic routeGraphic = new Graphic(route.getRouteGeometry(), lineSymbol);

      // Add the route graphic to the route layer
      mRouteOverlay.getGraphics().add(routeGraphic);
      // Add start and end pins
      final BitmapDrawable startPin = (BitmapDrawable) ContextCompat.getDrawable(getActivity(),R.drawable.route_pin_start) ;
      final BitmapDrawable endPin = (BitmapDrawable) ContextCompat.getDrawable(getActivity(),R.drawable.end_route_pin) ;
      // Current location from Google location services
      // needs a spatial reference before it can be added to map.
      final Point startPoint = new Point(mStart.getX(), mStart.getY(), mEnd.getSpatialReference());

      final Graphic begin = generateRoutePoints(startPoint, startPin);
      mRouteOverlay.getGraphics().add(begin);
      mRouteOverlay.getGraphics().add(generateRoutePoints(mEnd,endPin));


      // Zoom to the extent of the entire route with a padding
      final Envelope routingEnvelope = new Envelope(mStart.getX(),mStart.getY(), mEnd.getX(), mEnd.getY(), SpatialReferences.getWgs84());
      final Envelope projectedEnvelope = (Envelope) GeometryEngine.project(routingEnvelope, mMapView.getSpatialReference());
      mMapView.setViewpointGeometryAsync(projectedEnvelope, 100);

      // Get routing directions
      mRouteDirections = route.getDirectionManeuvers();

      // Show route header
      showRouteHeader(route.getTravelTime());

      if (mProgressDialog != null){
        mProgressDialog.dismiss();
      }
    }else{
      Log.i(TAG, "Route details haven't been set, no route to display");
    }
  }

  /**
   * Converts device specific pixels to density independent pixels.
   *
   * @param context - Activity Context
   * @param px - number of device specific pixels
   * @return number of density independent pixels
   */
  private float convertPixelsToDp(final Context context, final float px) {
    final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    return px / (metrics.densityDpi / 160f);
  }

  /**
   * Helper method for creating start/end graphic used
   * to display the route on the map
   * @param p - point representing location on the map
   * @param pin - BitmapDrawable to use for the returned graphic
   * @return - a graphic representing the point
   */
  private Graphic generateRoutePoints(final Point p, final BitmapDrawable pin){
    final float offsetY = convertPixelsToDp(getActivity(), pin.getBounds().bottom);
    final PictureMarkerSymbol symbol = new PictureMarkerSymbol(pin);
    symbol.setOffsetY(offsetY);
    return new Graphic(p, symbol);
  }
  /**
   *
   * @param presenter -  the MapContract.Presenter encapsulating
   * the business logic for the map.
   */
  @Override public final void setPresenter(final MapContract.Presenter presenter) {
    mPresenter = presenter;
  }

  /**
   * Given a map point, find the associated Place
   * @param p - a point representing a geolocation
   * @return - the place found at that geolocation
   */
  private Place getPlaceForPoint(final Point p){
    return mPresenter.findPlaceForPoint(p);
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
    public MapTouchListener(final Context context, final MapView mapView) {
      super(context, mapView);
    }
    @Override
    public final boolean onSingleTapConfirmed(final MotionEvent motionEvent) {
  //    removeNavigationCompletedListener();
      final android.graphics.Point screenPoint = new android.graphics.Point(
          (int) motionEvent.getX(),
          (int) motionEvent.getY());
      // identify graphics on the graphics overlay
      final ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphic = mMapView
          .identifyGraphicsOverlayAsync(mGraphicOverlay, screenPoint, 10, false);

      identifyGraphic.addDoneListener(new Runnable() {
        @Override
        public void run() {
          try {
            // get the list of graphics returned by identify
            final IdentifyGraphicsOverlayResult graphic = identifyGraphic.get();

            // get size of list in results
            final int identifyResultSize = graphic.getGraphics().size();
            if (identifyResultSize > 0){
              final Graphic foundGraphic = graphic.getGraphics().get(0);
              final Place foundPlace = getPlaceForPoint((Point)foundGraphic.getGeometry());
              if (foundPlace != null){
                showDetail(foundPlace);
              }
            }
          } catch (InterruptedException | ExecutionException ie) {
            Log.e(MapFragment.TAG, ie.getMessage());
          }
        }

      });
      return true;
    }
  }
}
