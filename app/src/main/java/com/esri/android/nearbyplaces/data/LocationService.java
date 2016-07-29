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
import android.util.Log;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.location.LocationDataSource;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorInfo;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;
/**
 * Created by sand8529 on 7/5/16.
 */
public class LocationService implements PlacesServiceApi {

  private final static String TAG = LocationService.class.getSimpleName();
  private static LocatorTask mLocatorTask;
  private static LocationService instance = null;


  protected LocationService(){}

  public static LocationService getInstance(){
    if (instance == null){
      instance = new LocationService();
    }
    return instance;
  }

  private Map<String,Place> mappedPlaces = new HashMap<>();


  public static void configureService(@NonNull String locatorUrl, @NonNull Runnable doneListener){
    checkNotNull(locatorUrl);
    checkNotNull(doneListener);
    if (null == mLocatorTask){
      mLocatorTask = new LocatorTask(locatorUrl);
      mLocatorTask.addDoneLoadingListener(doneListener);
      mLocatorTask.loadAsync();
    }
  }

  @Override public void getPlacesFromService(@NonNull GeocodeParameters parameters,@NonNull final PlacesServiceCallback callback)  {
    checkNotNull(parameters);
    checkNotNull(callback);
    String searchText = "";
    provisionOutputAttributes(parameters);
    provisionCategories(parameters);
    final ListenableFuture<List<GeocodeResult>> results = mLocatorTask.geocodeAsync(searchText, parameters);
    Log.i(TAG,"Geocode search started...");
    results.addDoneListener(new Runnable() {
      @Override public void run() {

        try {
          List<GeocodeResult> data = results.get();
          List<Place> places = new ArrayList<Place>();
          for (GeocodeResult r: data){
            Map<String,Object> attributes = r.getAttributes();
            String address = (String) attributes.get("Place_addr");
            String name = (String) attributes.get("PlaceName");
            String phone = (String) attributes.get("Phone");
            String url = (String) attributes.get("URL");
            String type = (String) attributes.get("Type");
            Point point = r.getDisplayLocation();
//            Set<String> keys = attributes.keySet();
//            for (String k: keys){
//              Log.i("ATTR", k + " " + attributes.get(k).toString());
//            }

            Place place = new Place(name, type, point, address, url,phone,null);
            Log.i("PLACE", place.toString());
            places.add(place);
            mappedPlaces.put(name,place);
          }
          callback.onLoaded(places);

        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  @Override public Place getPlaceDetail(String placeName) {
    Place foundPlace = null;
    if (!mappedPlaces.isEmpty()){
      if (mappedPlaces.get(placeName) != null){
        foundPlace = mappedPlaces.get(placeName);
      }
    }
    return foundPlace;
  }

  @Override public List<Place> getPlacesFromRepo() {
    return new ArrayList<>(mappedPlaces.values());
  }

  public LocatorInfo getLocatorInfo(){
    return mLocatorTask.getLocatorInfo();
  }

  private GeocodeParameters provisionCategories(@NonNull  GeocodeParameters parameters){
    checkNotNull(parameters);
    List<String> categories = parameters.getCategories();
    categories.add("Food");
    categories.add("Hotel");
    categories.add("Pizza");
    categories.add("Coffee Shop");
    categories.add("Bar or Pub");

    return parameters;
  }
  private GeocodeParameters provisionOutputAttributes(@NonNull GeocodeParameters parameters){
    checkNotNull(parameters);
    List<String> outputAttributes = parameters.getResultAttributeNames();
    outputAttributes.clear();
    outputAttributes.add("*");
    return parameters;
  }
}
