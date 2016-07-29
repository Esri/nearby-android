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
package com.esri.android.nearbyplaces.data;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of the Notes Service API that adds a latency simulating network.
 */

public class PlacesServiceApiImpl implements  PlacesServiceApi{

  private static final int SERVICE_LATENCY_IN_MILLIS = 2000;
  private static final ArrayMap<String,Place> PLACES_SERVICE_DATA = PlacesServiceApiEndpoint.loadPersistedPlaces();

  @Override public void getPlacesFromService(final @NonNull GeocodeParameters parameters, final @NonNull PlacesServiceCallback callback) {

    checkNotNull(callback);
    // Simulate network by delaying the execution.
    Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        List<Place> places = new ArrayList<>(PLACES_SERVICE_DATA.values());
        callback.onLoaded(places);
      }
    }, SERVICE_LATENCY_IN_MILLIS);
  }

  @Override public Place getPlaceDetail(String placeName) {
    return null;
  }

  @Override public List<Place> getPlacesFromRepo() {
    return null;
  }

}
