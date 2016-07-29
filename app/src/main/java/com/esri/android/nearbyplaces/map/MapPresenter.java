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

package com.esri.android.nearbyplaces.map;

import android.support.annotation.NonNull;
import android.util.Log;
import com.esri.android.nearbyplaces.data.LocationService;
import com.esri.android.nearbyplaces.data.Place;
import com.esri.android.nearbyplaces.data.PlacesServiceApi;
import com.esri.arcgisruntime.geometry.*;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
/**
 * Created by sand8529 on 6/24/16.
 */
public class MapPresenter implements MapContract.Presenter {

  private final static String TAG = MapPresenter.class.getSimpleName();
  private Point mLocation;

  private final MapContract.View mMapView;
  private LocationService mLocationService;
  private final static int MAX_RESULT_COUNT = 10;
  private final static String GEOCODE_URL = "http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer";

  public MapPresenter(@NonNull MapContract.View mapView ){
    mMapView = checkNotNull(mapView, "map view cannot be null");
    mMapView.setPresenter(this);
  }

  /**
   * Use the location service to geocode places of interest
   * based on the map's visible area extent.
   */
  @Override public void findPlacesNearby() {
    Point g =  mMapView.getMapView().getVisibleArea().getExtent().getCenter();

    if ( g !=null ){
      GeocodeParameters parameters = new GeocodeParameters();
      parameters.setMaxResults(MAX_RESULT_COUNT);
      parameters.setPreferredSearchLocation(g);
      mLocationService.getPlacesFromService(parameters, new PlacesServiceApi.PlacesServiceCallback() {
        @Override public void onLoaded(Object places) {
          List<Place> data = (List) places;

          // Create graphics for displaying locations in map
          mMapView.showNearbyPlaces(data);
        }
      });
    }
  }

  @Override public void centerOnPlace(Place p) {
    mMapView.centerOnPlace(p);
  }

  @Override public Place findPlaceForPoint(Point p) {
    Place foundPlace = null;
    List<Place> foundPlaces =mLocationService.getPlacesFromRepo();
    for (Place place : foundPlaces){
      if ((p).equals(place.getLocation())){
        foundPlace = place;
        break;
      }
    }
    return foundPlace;
  }
  /**
   * The entry point for this class starts
   * by loading the gecoding service.
   */
  @Override public void start() {
    mLocationService = LocationService.getInstance();
    List<Place> existingPlaces = mLocationService.getPlacesFromRepo();
    if (existingPlaces != null && existingPlaces.size()> 0){
      mMapView.showNearbyPlaces(existingPlaces);
    }else{
      LocationService.configureService(GEOCODE_URL,
          new Runnable() {
            @Override public void run() {
              findPlacesNearby();
            }
          });
    }

  }
}
