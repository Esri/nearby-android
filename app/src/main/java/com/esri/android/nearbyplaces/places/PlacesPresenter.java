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

import android.app.Activity;
import android.location.Location;
import android.support.annotation.NonNull;
import com.esri.android.nearbyplaces.data.LocationService;
import com.esri.android.nearbyplaces.data.Place;
import com.esri.android.nearbyplaces.data.PlacesServiceApi;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class PlacesPresenter implements PlacesContract.Presenter {


  private final PlacesContract.View mPlacesView;
  private Point mCurrentLocation = null;
  private Activity activity;

  private LocationService mLocationService;
  private final static int MAX_RESULT_COUNT = 10;
  private final static String GEOCODE_URL = "http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer";

  public PlacesPresenter( @NonNull PlacesContract.View listView){
    mPlacesView = checkNotNull(listView);
    mPlacesView.setPresenter(this);
  }

  /**
   * Place presenter starts by using the device
   * location as the initial parameter in the
   * geocode search.
   */
  @Override public void start() {
    mLocationService = LocationService.getInstance(activity);
    List<Place> existingPlaces = mLocationService.getPlacesFromRepo();
    if (existingPlaces != null && existingPlaces.size()> 0){
      setPlacesNearby(existingPlaces);
    }else{
      LocationService.configureService(GEOCODE_URL,
          new Runnable() {
            @Override public void run() {
              getPlacesNearby();
            }
          });
    }
  }

  @Override public void setContext(Activity a) {
    activity = a;
  }

  /**
   * Delegates the display of places to the view
   * @param places List<Place> items
   */
  @Override public void setPlacesNearby(List<Place> places) {
    mPlacesView.showNearbyPlaces(places);
  }

  @Override public void setLocation(Location location) {
    mCurrentLocation = new Point(location.getLongitude(), location.getLatitude());
  }

  @Override public void getPlacesNearby() {
    if (mCurrentLocation != null) {
      GeocodeParameters parameters = new GeocodeParameters();
      parameters.setMaxResults(MAX_RESULT_COUNT);
      parameters.setPreferredSearchLocation(mCurrentLocation);
      mLocationService.getPlacesFromService(parameters, new PlacesServiceApi.PlacesServiceCallback() {
        @Override public void onLoaded(Object places) {
          List<Place> data = (List) places;

          // Show list of places
          setPlacesNearby(data);
        }
      });
    }
  }

  @Override public Envelope getExtentForNearbyPlaces() {
    return mLocationService != null ? mLocationService.getResultEnveope(): null;
  }

}
