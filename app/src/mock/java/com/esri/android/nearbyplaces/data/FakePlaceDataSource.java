package com.esri.android.nearbyplaces.data;
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

import android.support.annotation.NonNull;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.google.common.collect.Lists;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
public class FakePlaceDataSource implements PlacesRepository {

  private static FakePlaceDataSource INSTANCE;

  private static final Map<GeocodeParameters, Place> PLACES_SERVICE_DATA = new LinkedHashMap<>();

  private FakePlaceDataSource(){}

  public static FakePlaceDataSource getInstance(){
    if (INSTANCE == null){
      INSTANCE = new FakePlaceDataSource();
    }
    return INSTANCE;
  }



  @Override public void getPlaces( @NonNull LoadPlacesCallback callback) {
    callback.onPlacesLoaded(Lists.newArrayList(PLACES_SERVICE_DATA.values()));
  }

  @Override public Place getPlaceDetail(String placeName) {
    return new Place("Powell's Books", "bookstore", new Point(45.521658, -122.7035132), "1055 W Burnside Portland, OR 97209",null, "(503) 228-4651", "NE",0);
  }
}
