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

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Concrete implementation to load places from the a data source.
 */
public class InMemoryPlacesRepository implements PlacesRepository {

  private final PlacesServiceApi mPlacesServiceApi;

  public InMemoryPlacesRepository(@NonNull PlacesServiceApi placesServiceApi){
    mPlacesServiceApi = placesServiceApi;
  }

  @VisibleForTesting
  List<Place> mCachedPlaces;

  @Override public void getPlaces(@NonNull final LoadPlacesCallback callback) {


    if (mCachedPlaces == null){
      mPlacesServiceApi.getPlacesFromService( new GeocodeParameters(), new PlacesServiceApi.PlacesServiceCallback<List<Place>>() {
        @Override public void onLoaded(List<Place> places) {
          mCachedPlaces = ImmutableList.copyOf(places);
          callback.onPlacesLoaded(mCachedPlaces);
        }
      });
    }else{
      callback.onPlacesLoaded(mCachedPlaces);
    }
  }

  @Override public Place getPlaceDetail(String placeName) {
    Place foundPlace = null;
    for (Place p: mCachedPlaces){
      if (p.getName().equalsIgnoreCase(placeName)){
        foundPlace = p;
        break;
      }
    }
    return foundPlace;
  }

}
