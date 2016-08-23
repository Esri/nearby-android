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
public class MapActivity extends AppCompatActivity {

  private static final String TAG = MapActivity.class.getSimpleName();
  private MapPresenter mMapPresenter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    Log.i("MapActivity", "Start_ON_CREATE");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.map_layout);

    // Set up the toolbar
    setUpToolbar();

    AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.map_appbar);

    // Sets the outline of the toolbar shadow to the background color
    // of the layout (transparent, in this case)
    appBarLayout.setOutlineProvider(ViewOutlineProvider.BACKGROUND);

    setUpFragments(savedInstanceState);

    setUpAuth();

    Log.i("MapActivity", "End_ON_CREATE");
  }

  private void setUpAuth(){
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
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu items for use in the action bar
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.map_menu, menu);
    return super.onCreateOptionsMenu(menu);
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
               mMapPresenter.getRoute();
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
}
