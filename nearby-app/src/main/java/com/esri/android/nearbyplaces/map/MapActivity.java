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

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.esri.android.nearbyplaces.R;
import com.esri.android.nearbyplaces.filter.FilterContract;
import com.esri.android.nearbyplaces.route.RouteDirectionsFragment;
import com.esri.android.nearbyplaces.util.ActivityUtils;
import com.esri.arcgisruntime.security.AuthenticationChallengeHandler;
import com.esri.arcgisruntime.security.AuthenticationManager;
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler;
import com.esri.arcgisruntime.tasks.networkanalysis.DirectionManeuver;

import java.util.List;

public class MapActivity extends AppCompatActivity implements FilterContract.FilterView {

  private MapPresenter mMapPresenter = null;

  @Override
  protected final void onCreate(final Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.map_layout);

    setUpMapFragment();

    setUpAuth();

  }

  /**
   * Authentication method used when requesting a route.
   */
  private void setUpAuth(){
    try{
      final AuthenticationChallengeHandler authenticationChallengeHandler = new DefaultAuthenticationChallengeHandler(this);
      AuthenticationManager.setAuthenticationChallengeHandler(authenticationChallengeHandler);
    }catch(final Exception e){
      Log.e("MapActivity", "Authentication handling issue: " + e.getMessage());
    }
  }
  /**
   * When user presses 'Apply' button in filter dialog,
   * re-filter results.
   * @param applyFilter - boolean
   */
  @Override public final void onFilterDialogClose(final boolean applyFilter) {
    if (applyFilter){
      mMapPresenter.start();
    }
  }


  /**
   * Set up map fragment
   */
  private void setUpMapFragment(){

    final FragmentManager fm = getSupportFragmentManager();
    MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map_fragment_container);

    if (mapFragment == null) {
      mapFragment = MapFragment.newInstance();
      ActivityUtils.addFragmentToActivity(
          getSupportFragmentManager(), mapFragment, R.id.map_fragment_container, getString(R.string.map_fragment));
    }
    mMapPresenter = new MapPresenter(mapFragment);

  }

  /**
   * Show the list of directions
   * @param directions List of DirectionManeuver items containing navigation directions
   */
  public final void showDirections(final List<DirectionManeuver> directions){
    final FragmentManager fm = getSupportFragmentManager();
    RouteDirectionsFragment routeDirectionsFragment = (RouteDirectionsFragment) fm.findFragmentById(R.id.route_directions_container);
    if (routeDirectionsFragment == null){
      routeDirectionsFragment = RouteDirectionsFragment.newInstance();
      ActivityUtils.addFragmentToActivity(
          getSupportFragmentManager(), routeDirectionsFragment, R.id.route_directions_container, getString(R.string.route_fragment));
    }
    // Show the fragment
    final LinearLayout layout = (LinearLayout) findViewById(R.id.route_directions_container);
    layout.setLayoutParams(new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT));
    layout.requestLayout();

    // Hide the map
    final FrameLayout mapLayout = (FrameLayout) findViewById(R.id.map_fragment_container);
    final CoordinatorLayout.LayoutParams  layoutParams  =  new CoordinatorLayout.LayoutParams(0,0);
    layoutParams.setMargins(0, 0,0,0);
    mapLayout.setLayoutParams(layoutParams);
    mapLayout.requestLayout();

    routeDirectionsFragment.setRoutingDirections(directions);
  }

  /**
   * Display the specific directions for the segment of the route
   * the user has clicked on.
   * @param position An integer representing the index in the
   *                 list of segment directions.
   */
  public final void showRouteDetail(final int position){
    // Hide the list of directions
    final LinearLayout layout = (LinearLayout) findViewById(R.id.route_directions_container);
    layout.setLayoutParams(new CoordinatorLayout.LayoutParams(0,0));
    layout.requestLayout();

    // Restore the map, removing any route segment detail
    final MapFragment  mapFragment = restoreMapAndRemoveRouteDetail();
    // Add specific route segment detail
    mapFragment.showRouteDetail(position);
  }

  /**
   * Restore map to original size and remove
   * views associated with displaying route segments.
   * @return MapFragment - The fragment containing the map
   */
  public final MapFragment restoreMapAndRemoveRouteDetail(){
    // Remove the route directions
    final LinearLayout layout = (LinearLayout) findViewById(R.id.route_directions_container);
    layout.setLayoutParams(new CoordinatorLayout.LayoutParams(0, 0));
    layout.requestLayout();

    // Show the map
    final FrameLayout mapLayout = (FrameLayout) findViewById(R.id.map_fragment_container);

    final CoordinatorLayout.LayoutParams  layoutParams  =  new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);
    mapLayout.setLayoutParams(layoutParams);
    mapLayout.requestLayout();

    final FragmentManager fm = getSupportFragmentManager();
    final MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map_fragment_container);
    mapFragment.removeRouteDetail();
    return mapFragment;
  }

  /**
   * Adjust the map view and show route
   */
  public final void restoreRouteView(){
    MapFragment mapFragment = restoreMapAndRemoveRouteDetail();
    mapFragment.displayRoute();
  }

  @Override
  public void onBackPressed() {
    int count = getSupportFragmentManager().getBackStackEntryCount();
    if (count == 0) {
      super.onBackPressed();
    } else {
      getSupportFragmentManager().popBackStack();
      FragmentManager.BackStackEntry entry = getSupportFragmentManager().getBackStackEntryAt(count-1);
      String fragmentName = entry.getName();
      if (fragmentName.equalsIgnoreCase(getString(R.string.route_fragment))){
        restoreRouteView();
      }else{
        finish();
      }
    }
  }
}
