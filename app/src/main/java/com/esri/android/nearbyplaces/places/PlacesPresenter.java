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

package com.esri.android.nearbyplaces.places;

import android.support.annotation.NonNull;
import com.esri.android.nearbyplaces.data.LocationService;
import com.esri.android.nearbyplaces.data.Place;
import com.esri.android.nearbyplaces.map.MapContract;
import com.esri.android.nearbyplaces.mapplace.MapPlaceContract;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by sand8529 on 6/16/16.
 */
public class PlacesPresenter implements PlacesContract.Presenter {


  private final PlacesContract.View mPlacesView;

  public PlacesPresenter( @NonNull PlacesContract.View listView){
    mPlacesView = checkNotNull(listView);
    mPlacesView.setPresenter(this);
  }


  @Override public void start() {
    LocationService locationService = LocationService.getInstance();
    List<Place> places = locationService.getPlacesFromRepo();
    setPlacesNearby(places);
  }

  /**
   * Delegates the display of places to the view
   * @param places List<Place> items
   */
  @Override public void setPlacesNearby(List<Place> places) {
    mPlacesView.showNearbyPlaces(places);
  }
}
