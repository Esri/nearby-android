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

import android.content.Context;
import android.support.annotation.NonNull;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult;

import java.util.List;

/**
 * Main entry point for accessing places data.
 * Defines an interface to the service API that is used by this application. All data request should
 * be piped through this interface.
 */

public interface PlacesServiceApi {

  interface PlacesServiceCallback<List>{  // callback from server
    void onLoaded(List places);
  }

  interface RouteServiceCallback{
    void onRouteReturned(RouteResult result);
  }

  void getRouteFromService(Point start, Point end, Context context, RouteServiceCallback callback);

  void getPlacesFromService(@NonNull GeocodeParameters parameters, @NonNull PlacesServiceCallback callback);

  List<Place> getPlacesFromRepo();
}
