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
import com.esri.android.nearbyplaces.data.LocationService;
import com.esri.android.nearbyplaces.data.Place;
import com.esri.android.nearbyplaces.data.PlacesServiceApi;
import com.esri.android.nearbyplaces.data.TravelMode;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult;
import com.esri.arcgisruntime.tasks.networkanalysis.Stop;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the concrete implementation of the Presenter defined in the MapContract.
 * It encapsulates business logic and drives the behavior of the View.
 */
public class MapPresenter implements MapContract.Presenter {

  private final static String TAG = MapPresenter.class.getSimpleName();

  private final MapContract.View mMapView;
  private List<Place> mStops = new ArrayList<>();
  private LocationService mLocationService;
  private final static int MAX_RESULT_COUNT = 50;
  private final static String GEOCODE_URL = "http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer";
  private Place mCenteredPlace;
  private String mTravelMode = TravelMode.TravelModeTypes.Walk.name();

  public MapPresenter(@NonNull final MapContract.View mapView ){
    mMapView = mapView;
    mMapView.setPresenter(this);
  }

  @Override public final void findPlacesNearby() {
    mMapView.showProgressIndicator("Finding nearby places...");
    final Point g =  mMapView.getMapView().getVisibleArea().getExtent().getCenter();

    if ( g !=null ){
      final GeocodeParameters parameters = new GeocodeParameters();
      parameters.setMaxResults(MAX_RESULT_COUNT);
      parameters.setPreferredSearchLocation(g);
      mLocationService.getPlacesFromService(parameters, new PlacesServiceApi.PlacesServiceCallback() {
        @Override public void onLoaded(final Object places) {
          final List<Place> data = (List) places;

          // Create graphics for displaying locations in map
          mMapView.showNearbyPlaces(data);
        }
      });
    }
  }

  @Override public final void centerOnPlace(final Place p) {
    mCenteredPlace = p;
    mMapView.centerOnPlace(mCenteredPlace);
  }

  @Override public final Place findPlaceForPoint(final Point p) {
    Place foundPlace = null;
    final List<Place> foundPlaces = mLocationService.getPlacesFromRepo();
    for (final Place place : foundPlaces){
      if (p.equals(place.getLocation())){
        foundPlace = place;
        break;
      }
    }
    return foundPlace;
  }

  @Override public final void getRoute() {
    if ((mCenteredPlace != null) && (mLocationService.getCurrentLocation() != null)){
      mMapView.showProgressIndicator("Retrieving route...");
      List<Stop> stops = new ArrayList<>();
      if (mStops.size() > 0) {
        for (Place place : mStops) {
          stops.add(new Stop(place.getLocation()));
        }
      }
      mMapView.getRoute(mLocationService, stops, mTravelMode);

      // Clear out stops
      mStops.clear();
    }
  }

  @Override public void setCurrentExtent(final Envelope envelope) {
    mLocationService.setCurrentEnvelope(envelope);
  }

  @Override public void addStop(Place p) {
    if (mStops.contains(p)  && !p.isStop()) {
      mStops.remove(p);
    } else {
      mStops.add(p);
    }
  }

  @Override
  public void setTravelMode(String mode) {
    mTravelMode = mode;
  }

  @Override
  public TravelMode.TravelModeTypes getTravelMode() {
    if (mTravelMode.equalsIgnoreCase(TravelMode.TravelModeTypes.Drive.name())){
      return TravelMode.TravelModeTypes.Drive;
    } else {
      return TravelMode.TravelModeTypes.Walk;
    }
  }

  /**
   * The entry point for this class starts
   * by loading the geocoding service.
   */
  @Override public final void start() {
    mLocationService = LocationService.getInstance();
    final List<Place> existingPlaces = mLocationService.getPlacesFromRepo();
    if (existingPlaces != null){
      mMapView.showNearbyPlaces(existingPlaces);
    }else{
      LocationService.configureService(GEOCODE_URL,
          // On locator task load success
          new Runnable() {
            @Override public void run() {
              findPlacesNearby();
            }
          },
          // On locator task load error
          new Runnable() {
            @Override public void run() {
              mMapView.showMessage("The locator task was unable to load");
            }
          });
    }
  }

}
