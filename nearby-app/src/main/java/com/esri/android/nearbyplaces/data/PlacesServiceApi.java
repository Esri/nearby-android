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
import com.esri.arcgisruntime.tasks.networkanalysis.Stop;

import java.util.List;

/**
 * A set of interfaces to the service API that is used by this application.
 * All data request should be piped through this interface.
 */

public interface PlacesServiceApi {

  interface PlacesServiceCallback<List>{
    /**
     * Once the aync geocoding service is completed, a list of places is returned
     * @param places -
     */
    void onLoaded(List places);
  }

  interface RouteServiceCallback{
    /**
     * A callback method used to return an asyn route request
     * @param result - RouteResult
     */
    void onRouteReturned(RouteResult result);
  }

  /**
   * Given a start, end, optional stops and travel mode,
   * return a route to the provided callback.
   * @param start - Point
   * @param end - Point
   * @param context - Context
   * @param callback - RouteServiceCallback
   * @param stops - List<Stop></Stop>
   * @param travelMode - String
   */
  void getRouteFromService(Point start, Point end, Context context,
                           RouteServiceCallback callback,
                           List<Stop> stops, String travelMode);

  /**
   * An aync call to obtain a list of places given a set of geocode parameters
   * @param parameters - GeocodeParameters
   * @param callback - PlacesServiceCallback
   */
  void getPlacesFromService(@NonNull GeocodeParameters parameters,
                            @NonNull PlacesServiceCallback callback);

  /**
   * Return a place given a place name
   * @param placeName - String
   * @return - Place
   */
  Place getPlaceDetail(String placeName);

  /**
   * Return list of Place items from a local cache
   * @return - List<Place></Place>
   */
  List<Place> getPlacesFromRepo();

}
