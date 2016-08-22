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
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.esri.android.nearbyplaces.NearbyPlaces;
import com.esri.android.nearbyplaces.PlaceListener;
import com.esri.android.nearbyplaces.R;
import com.esri.android.nearbyplaces.data.CategoryHelper;
import com.esri.android.nearbyplaces.data.Place;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.*;
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.*;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by sand8529 on 6/24/16.
 */
public class MapFragment extends Fragment implements  MapContract.View {

  private MapContract.Presenter mPresenter;

  private FrameLayout mMapLayout;

  private MapView mMapView;

  private LocationDisplay mLocationDisplay;

  private GraphicsOverlay mGraphicOverlay;


  private boolean initialLocationLoaded =false;

  private boolean mCenteringOnPlace = false;

  private Graphic mCenteredGraphic = null;

  private Place mCenteredPlace = null;

  private PlaceListener mCallback;

  private NavigationCompletedListener mNavigationCompletedListener;

  private final static String TAG = MapFragment.class.getSimpleName();

  private long mStartTime;

  private String centeredPlaceName;

  private Point mCurrentLocation = null;

  public MapFragment(){}

  public static MapFragment newInstance(){
    return new MapFragment();
  }

  @Override
  public void onCreate(@NonNull Bundle savedInstance){

    super.onCreate(savedInstance);
    // retain this fragment
    setRetainInstance(true);
  }

  @Override
  @Nullable
  public View onCreateView(LayoutInflater layoutInflater, ViewGroup container,
      Bundle savedInstance){
    mStartTime = Calendar.getInstance().getTimeInMillis();
    if (getArguments() != null){
      Bundle arguments = getArguments();
      double latitdue = arguments.getDouble(NearbyPlaces.LATITUDE);
      double longitude = arguments.getDouble(NearbyPlaces.LONGITUDE);
      Point location = new Point(longitude, latitdue, SpatialReferences.getWebMercator());
      mCurrentLocation = location;
    }
    Log.i("MapFragment", "Start_ON_CREATE_VIEW");
    View root = layoutInflater.inflate(R.layout.map_fragment, container,false);
    setUpMapView(root);
    Log.i("MapFragment", "End_ON_CREATE_VIEW");

    // If any extra data was sent, store it.
    if (getActivity().getIntent().getSerializableExtra("PLACE_DETAIL") != null){
      centeredPlaceName = getActivity().getIntent().getStringExtra("PLACE_DETAIL");
    }

    return root;
  }

  /**
   * Add the map to the view and set up location display
   * @param root View
   */
  private void setUpMapView(View root){
    Log.i("MapFragment", "Start_SET_UP_MAP_VIEW");

    mMapView = (MapView) root.findViewById(R.id.map);
    Log.i("MapFragment", "Find_map_view");

    Basemap basemap = new Basemap(new ArcGISVectorTiledLayer(
        getResources().getString(R.string.navigation_url)));
    Log.i("MapFragment", "Basemap_created");

    ArcGISMap map = new ArcGISMap(basemap);
    mMapView.setMap(map);
    Log.i("MapFragment", "Map_attached_to_map_view");

    // Add graphics overlay for map markers
    mGraphicOverlay  = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(mGraphicOverlay);
    Log.i("MapFragment", "Graphics_overlay_added");

    mLocationDisplay = mMapView.getLocationDisplay();
    Log.i("MapFragment", "Get_location_display");

    mLocationDisplay.startAsync();
    Log.i("MapFragment", "Start_async_loc_display");


    mMapView.addDrawStatusChangedListener(new DrawStatusChangedListener() {
      @Override public void drawStatusChanged(DrawStatusChangedEvent drawStatusChangedEvent) {
        if (drawStatusChangedEvent.getDrawStatus() == DrawStatus.COMPLETED){
          Log.i("MapFragment", "DRAW_COMPLETE, spatial reference " + mMapView.getSpatialReference().getWKText());
        //  mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
         // Log.i("MapFragment", "Start_auto_pan_mode");
          long elapsedTime = (Calendar.getInstance().getTimeInMillis() - mStartTime)/1000;
          Log.i("MapFragment", "Time taken = " + Long.toString(elapsedTime));
          mPresenter.start();
          mMapView.removeDrawStatusChangedListener(this);

        }
      }
    });

    // Setup OnTouchListener to detect and act on long-press
    mMapView.setOnTouchListener(new MapTouchListener(getActivity().getApplicationContext(), mMapView));
    Log.i("MapFragment", "End_SET_UP_MAP_VIEW");
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    // This makes sure that the container activity has implemented
    // the callback interface. If not, it throws an exception
    try {
      mCallback = (PlaceListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(context
          + " must implement PlacesListener");
    }
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    // Always call the superclass so it can save the view hierarchy state
    super.onSaveInstanceState(savedInstanceState);
  }
  /**
   * Attach the viewpoint change listener
   * so that POIs get updated as map's
   * visible area is changed.
   */
  private void setNavigationCompletedListener(){
    mNavigationCompletedListener = new NavigationCompletedListener() {
      @Override public void navigationCompleted(NavigationCompletedEvent navigationCompletedEvent) {
          mCallback.onMapScroll();
      }
    };
    mMapView.addNavigationCompletedListener(mNavigationCompletedListener);
    Log.i(TAG, "Navigation complete handler ON");
  }

  private void removeNavigationCompletedListener(){
    if (mNavigationCompletedListener != null){
      mMapView.removeNavigationCompletedListener(mNavigationCompletedListener);
      mNavigationCompletedListener = null;
    }
    Log.i(TAG, "Navigation complete handler OFF");
  }

  @Override
  public void onResume(){
    super.onResume();
    mMapView.resume();
   if (!mLocationDisplay.isStarted()){
      mLocationDisplay.startAsync();
    }
    Log.i(TAG, "Map fragment onResume " + "and location display is " + mLocationDisplay.isStarted());
//  //  mPresenter.start();
  }

  @Override
  public void onPause(){
    super.onPause();
    mMapView.pause();
   if (mLocationDisplay.isStarted()){
      mLocationDisplay.stop();
    }
    Log.i(TAG, "Map fragment onPause " + "and location display is " + mLocationDisplay.isStarted());

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
    // and create a list of points
    List<Point> points = new ArrayList<>();
    for (Place place : places){
      BitmapDrawable pin = (BitmapDrawable) ContextCompat.getDrawable(getActivity(),getDrawableForPlace(place)) ;
      final PictureMarkerSymbol pinSymbol = new PictureMarkerSymbol(pin);
      Point graphicPoint = place.getLocation();
      Graphic graphic = new Graphic(graphicPoint, pinSymbol);
      points.add(graphicPoint);
      mGraphicOverlay.getGraphics().add(graphic);
    }
    Envelope env = determineResultEnvelope(points);
    mMapView.setViewpoint(new Viewpoint(env));

    // If a centered place name is not null,
    // show detail view
    if (centeredPlaceName != null){
      for (Place p: places){
        if (p.getName().equalsIgnoreCase(centeredPlaceName)){
          ((MapActivity)getActivity()).showDetail(p);
          centeredPlaceName = null;
          break;
        }
      }
    }
  }
  private Envelope determineResultEnvelope(List<Point> points){

    Multipoint mp = new Multipoint(points);
    return GeometryEngine.buffer(mp,0.0007).getExtent();


  }
  /**
   * Assign appropriate drawable given place type
   * @param p - Place
   * @return - Drawable
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

//  @Override public LocationDisplay getLocationDisplay() {
//    return mLocationDisplay;
//  }

  /**
   * Center the selected place and change the pin
   * color to blue.
   * @param p
   */
  @Override public void centerOnPlace(Place p) {
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
          Log.i(TAG, "Done centering");
          setNavigationCompletedListener();
        }
      }
    });
    // Change the pin icon
    if (mCenteredGraphic != null){
      BitmapDrawable oldPin = (BitmapDrawable) ContextCompat.getDrawable(getActivity(),getDrawableForPlace(mCenteredPlace)) ;
      mCenteredGraphic.setSymbol(new PictureMarkerSymbol(oldPin));
    }
    List<Graphic> graphics = mGraphicOverlay.getGraphics();
    for (Graphic g : graphics){
      if (g.getGeometry().equals(p.getLocation())){
        mCenteredGraphic = g;
        BitmapDrawable pin = (BitmapDrawable) ContextCompat.getDrawable(getActivity(),getPinForCenterPlace(p)) ;
        final PictureMarkerSymbol pinSymbol = new PictureMarkerSymbol(pin);
        g.setSymbol(pinSymbol);
        break;
      }
    }
    // Keep track of centered place since
    // it will be needed to reset
    // the graphic if another place
    // is centered.
    mCenteredPlace = p;
  }

  @Override public void setCurrentLocation(Point location) {
    mMapView.setViewpointCenterAsync(location);
  }

  /**
   *
   * @param presenter
   */
  @Override public void setPresenter(MapContract.Presenter presenter) {
    mPresenter = presenter;
  }

  /**
   * Given a map point, find the associated Place
   */
  private Place findPlaceForPoint(Point p){
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
              Place foundPlace = findPlaceForPoint((Point)foundGraphic.getGeometry());
              if (foundPlace != null){
                mCallback.showDetail(foundPlace);
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
