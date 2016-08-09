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
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;

import java.util.List;

/**
 * Main entry point for accessing places data.
 * <p>
 * For simplicity, only getPlacesFromService() and getPlace() have callbacks. Consider adding callbacks to other
 * methods to inform the user of network/database errors or successful operations.
 * For example, when a new place is saved, it's synchronously stored in cache but usually every
 * operation on database or network should be executed in a different thread.
 *
 * Defines an interface to the service API that is used by this application. All data request should
 * be piped through this interface.
 */

public interface PlacesServiceApi {

  interface PlacesServiceCallback<List>{  // callback from server

   void onLoaded(List places);

  }


  void getPlacesFromService(@NonNull GeocodeParameters parameters, @NonNull PlacesServiceCallback callback);
  Place getPlaceDetail(String placeName);

  List<Place> getPlacesFromRepo();

}
