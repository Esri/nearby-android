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

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.esri.android.nearbyplaces.BuildConfig;
import com.esri.android.nearbyplaces.NearbyPlaces;
import com.esri.android.nearbyplaces.PlaceListener;
import com.esri.android.nearbyplaces.R;
import com.esri.android.nearbyplaces.data.CategoryHelper;
import com.esri.android.nearbyplaces.data.LocationService;
import com.esri.android.nearbyplaces.data.Place;
import com.esri.android.nearbyplaces.filter.FilterContract;
import com.esri.android.nearbyplaces.filter.FilterDialogFragment;
import com.esri.android.nearbyplaces.filter.FilterPresenter;
import com.esri.android.nearbyplaces.places.PlacesActivity;
import com.esri.android.nearbyplaces.util.ActivityUtils;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.security.AuthenticationManager;
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler;
import com.esri.arcgisruntime.security.OAuthConfiguration;
import com.esri.arcgisruntime.tasks.route.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by sand8529 on 7/27/16.
 */
public class MapActivity extends AppCompatActivity implements PlaceListener, FilterContract.FilterView {

  private BottomSheetBehavior bottomSheetBehavior;
  private static final String TAG = MapActivity.class.getSimpleName();
  private FrameLayout mBottomSheet;
  private CoordinatorLayout mMapLayout;
  private boolean mShowSnackbar = false;
  private MapPresenter mMapPresenter;
  private List placesFound;
  private Place selectedPlace;
  private RouteTask mRouteTask;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    Log.i("MapActivity", "Start_ON_CREATE");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.map_layout);

    mMapLayout = (CoordinatorLayout) findViewById(R.id.map_coordinator_layout);

    // Set up the toolbar
    setUpToolbar();


    AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.map_appbar);

    // Sets the outline of the toolbar shadow to the background color
    // of the layout (transparent, in this case)
    appBarLayout.setOutlineProvider(ViewOutlineProvider.BACKGROUND);

    //Set up behavior for the bottom sheet
    bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_card_view));

    bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
      @Override
      public void onStateChanged(View bottomSheet, int newState) {
        invalidateOptionsMenu();
        if (newState == BottomSheetBehavior.STATE_COLLAPSED && mShowSnackbar) {
          showSearchSnackbar();
          mShowSnackbar = false;
        }
      }

      @Override
      public void onSlide(View bottomSheet, float slideOffset) {
      }
    });

    mBottomSheet = (FrameLayout) findViewById(R.id.bottom_card_view);



    setUpFragments(savedInstanceState);
    try{
      OAuthConfiguration oauthConfig = new OAuthConfiguration(
          "https://www.arcgis.com", BuildConfig.CLIENT_ID, BuildConfig.OAUTH_REDIRECT_ID);
      Log.i("LocationService", oauthConfig.getClientId());
    //  AuthenticationManager.addOAuthConfiguration(oauthConfig);
      DefaultAuthenticationChallengeHandler authenticationChallengeHandler = new DefaultAuthenticationChallengeHandler(this);
      AuthenticationManager.setAuthenticationChallengeHandler(authenticationChallengeHandler);
    }catch(Exception e){
      Log.e("MapActivity", e.getMessage());
    }


    Log.i("MapActivity", "End_ON_CREATE");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu items for use in the action bar
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.map_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }
  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    // Always call the superclass so it can save the view hierarchy state
    super.onSaveInstanceState(savedInstanceState);
  }


  private void setUpToolbar(){
    Toolbar toolbar = (Toolbar) findViewById(R.id.map_toolbar);
    setSupportActionBar(toolbar);
    toolbar.setTitle("");
    final ActionBar ab = getSupportActionBar();
    ab.setDisplayHomeAsUpEnabled(true);
    final Activity a = this;
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
               dialogFragment.show(getFragmentManager(),"dialog_fragment");

             }
             if (item.getTitle().toString().equalsIgnoreCase("Route")){
               MapView mapView = (MapView) findViewById(R.id.map);

               LocationService locationService = LocationService.getInstance();
               final Point start = locationService.getCurrentLocation();
               mRouteTask= new RouteTask(getString(R.string.routingservice_url));
               mRouteTask.addDoneLoadingListener(new RouteSolver(start,selectedPlace.getLocation()));
               mRouteTask.loadAsync();
             }
             return false;
           }
         });
  }

  private void showList(){
    Intent intent = new Intent(this, PlacesActivity.class);

    startActivity(intent);
  }
  /**
   * Set up fragments
   */
  private void setUpFragments(Bundle savedInstanceState){

    FragmentManager fm = getSupportFragmentManager();
    MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map_fragment_container);

    if (mapFragment == null) {
      mapFragment = MapFragment.newInstance();
    }
    ActivityUtils.addFragmentToActivity(
        getSupportFragmentManager(), mapFragment, R.id.map_fragment_container, "map fragment");

    mMapPresenter = new MapPresenter(mapFragment);

  }

  private void showSearchSnackbar(){
    // Show snackbar prompting user about
    // scanning for new locations
    Snackbar snackbar = Snackbar
        .make(mMapLayout, "Search for places?", Snackbar.LENGTH_LONG)
        .setAction("SEARCH", new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            mMapPresenter.findPlacesNearby();
          }
        });

    snackbar.show();
  }

  @Override public void onPlacesFound(List<Place> places) {

  }

  @Override public void onPlaceSearch() {

  }

  /**
   * @param place
   */
  @Override public void showDetail(Place place) {
    selectedPlace = place;
    // Get the menu item and show the map
    //invalidateOptionsMenu();

    // Change the background of the app bar layout
    // and add icons for closing detail and
    // requesting routing
    //toggleAppBarLayout(true);

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
    Drawable d =   CategoryHelper.getDrawableForPlace(place, this) ;
    ImageView icon = (ImageView) findViewById(R.id.TypeIcon);
    icon.setImageDrawable(d);
    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    // Center map on selected place
    mMapPresenter.centerOnPlace(place);
    mShowSnackbar = false;
  }

  @Override public void onMapScroll() {
    //Dismiss bottom sheet
    mShowSnackbar = true;
    if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){ // show snackbar prompting for re-doing search
      showSearchSnackbar();
    }else{
      bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

  }

  /**
   * Set the menu options based on
   * the bottom sheet state
   *
   */
  @Override
  public boolean onPrepareOptionsMenu(Menu menu){
    MenuItem listItem = menu.findItem(R.id.list_action);
    MenuItem routeItem = menu.findItem(R.id.route_action);
    if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
      listItem.setVisible(true);
      routeItem.setVisible(false);
    }else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
      listItem.setVisible(false);
      routeItem.setVisible(true);
    }
    return super.onPrepareOptionsMenu(menu);
  }

  /**
   * When user presses 'Apply' button in filter dialong,
   * re-filter results.
   * @param applyFilter - boolean
   */
  @Override public void onFilterDialogClose(boolean applyFilter) {
    if (applyFilter){
      mMapPresenter.start();
    }

  }
  /**
   * A helper class for solving routes
   */
  private class RouteSolver implements Runnable{
    private final Stop origin;

    private final Stop destination;

    public RouteSolver(Point start, Point end){
      origin = new Stop(start);
      destination = new Stop(end);
    }
    @Override
    public void run (){
      LoadStatus status = mRouteTask.getLoadStatus();
      Log.i(TAG, "Route task is " + status.name());

      // Has the route task loaded successfully?
      if (status == LoadStatus.FAILED_TO_LOAD) {
        Log.i(TAG, mRouteTask.getLoadError().getMessage());

      } else {

        final ListenableFuture<RouteParameters> routeTaskFuture = mRouteTask
            .generateDefaultParametersAsync();
        // Add a done listener that uses the returned route parameters
        // to build up a specific request for the route we need
        routeTaskFuture.addDoneListener(new Runnable() {

          @Override
          public void run() {
            try {
              RouteParameters routeParameters = routeTaskFuture.get();
              // Add a stop for origin and destination
              routeParameters.getStops().add(origin);
              routeParameters.getStops().add(destination);
              Log.i(TAG, "Origin x/y = " + origin.getGeometry().getX()+ ", " + origin.getGeometry().getY());
              Log.i(TAG, "Destination x/y= " + destination.getGeometry().getX() + ", " + destination.getGeometry().getY());
              // We want the task to return driving directions and routes
              routeParameters.setReturnDirections(true);
              routeParameters.setDirectionsDistanceTextUnits(
                  DirectionDistanceTextUnits.IMPERIAL);
              routeParameters.setOutputSpatialReference(SpatialReferences.getWebMercator());

              final ListenableFuture<RouteResult> routeResFuture = mRouteTask
                  .solveAsync(routeParameters);
              routeResFuture.addDoneListener(new Runnable() {
                @Override
                public void run() {
                  try {
                    RouteResult routeResult = routeResFuture.get();
                    // Show route results
                    if (routeResult != null){
                      Log.i(TAG, "Got a result");
                    }else{
                      Log.i(TAG, "NO RESULT FROM ROUTING");
                    }



                  } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                  }
                }
              });
            } catch (Exception e1){
              Log.e(TAG,e1.getMessage() );
            }
          }
        });
      }
    }
  }
}
