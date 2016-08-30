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

import android.support.v4.util.ArrayMap;
import com.esri.arcgisruntime.geometry.Point;

/**
 * This is the endpoint for your data source. Typically, it would be a SQLite db and/or a server
 * API. In this example, we fake this by creating the data on the fly.
 */
public final class PlacesServiceApiEndpoint {

  private final static ArrayMap<String,Place> DATA;

  static {
    DATA = new ArrayMap(2);
    addPlace("Powell's Books", "bookstore", new Point(45.521658, -122.7035132), "1055 W Burnside Portland, OR 97209",null, "(503) 228-4651");
    addPlace("Portland Japanese Garden", "garden", new Point(45.5188089, -122.7101633 ), "611 SW Kingston Ave, Portland, OR 97205", "japanesegarden.com","(503) 223-1321");
  }

  private static void addPlace(String placeName, String type, Point location, String address, String URL, String phone){
    Place newPlace = new Place(placeName, type, location, address, URL, phone,null,0);
    DATA.put(newPlace.getName(), newPlace);
  }

  /**
   * @return the Places to show when starting the app.
   */
  public static ArrayMap<String,Place> loadPersistedPlaces(){
    return DATA;
  }
}
