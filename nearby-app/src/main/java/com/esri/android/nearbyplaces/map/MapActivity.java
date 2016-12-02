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
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.esri.android.nearbyplaces.R;
import com.esri.android.nearbyplaces.filter.FilterContract;
import com.esri.android.nearbyplaces.util.ActivityUtils;
import com.esri.arcgisruntime.security.AuthenticationChallengeHandler;
import com.esri.arcgisruntime.security.AuthenticationManager;
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler;

public class MapActivity extends AppCompatActivity implements FilterContract.FilterView {

  private MapPresenter mMapPresenter = null;

  @Override
  protected final void onCreate(final Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.map_layout);

    setUpFragments();

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
   * Set up fragments
   */
  private void setUpFragments(){

    final FragmentManager fm = getSupportFragmentManager();
    MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map_fragment_container);

    if (mapFragment == null) {
      mapFragment = MapFragment.newInstance();
      ActivityUtils.addFragmentToActivity(
          getSupportFragmentManager(), mapFragment, R.id.map_fragment_container, "map fragment");
    }

    mMapPresenter = new MapPresenter(mapFragment);

  }
}
