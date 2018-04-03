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

import com.esri.android.nearbyplaces.BasePresenter;
import com.esri.android.nearbyplaces.BaseView;
import com.esri.android.nearbyplaces.data.LocationService;
import com.esri.android.nearbyplaces.data.Place;
import com.esri.android.nearbyplaces.data.TravelMode;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult;
import com.esri.arcgisruntime.tasks.networkanalysis.Stop;

import java.util.List;

/**
 * This is the contract between the Presenter and View components of the MVP pattern.
 * It defines methods and logic used when showing the map.
 */
public interface MapContract {

  interface View extends BaseView<Presenter>{

    /**
     * Display places on the map
     * @param placeList - List<Place></Place>
     */
    void showNearbyPlaces(List<Place> placeList);

    /**
     * Return the view
     * @return - MapView
     */
    MapView getMapView();

    /**
     * Center map on given place.
     * @param p - Place
     */
    void centerOnPlace(Place p);

    /**
     * Set the RouteResult in the view
     * @param routeResult - RouteResult
     * @param start - Point
     * @param end - Point
     * @param stops - List<Stop></Stop>
     */
    void setRoute(RouteResult routeResult, Point start, Point end, List<Stop> stops);

    /**
     * Display message to user
     * @param message - String
     */
    void showMessage(String message);

    /**
     * Show a progress indicator with given message
     * @param message - String
     */
    void showProgressIndicator(String message);

    /**
     * Highlight a step in the route
     * @param position - int representing a specific index in the route directions
     */
    void showRouteDetail(int position);

    /**
     * Remove any other views blocking the map
     */
    void restoreMapView();

    /**
     * Obtain a route for given stops and travel mode
     * @param service - LocationService
     * @param stops - List<Stop></Stop>
     * @param mode - String representing travel mode
     */
    void getRoute(LocationService service, List<Stop> stops, String mode);

    /**
     * Show/hide the device location floating action button
     * @param show - boolean
     */
    void showFAB(boolean show);

  }

  interface Presenter extends BasePresenter {

    /**
     * Find places near the center of the map
     */
    void findPlacesNearby();

    /**
     * Center map view on a particular place
     * @param p - Place
     */
    void centerOnPlace(Place p);

    /**
     * Return a place for given point
     * @param p - Point
     * @return - Place
     */
    Place findPlaceForPoint(Point p);

    /**
     * Get route to selected place from current device location
     */
    void getRoute();

    /**
     * Set the envelope based on map's current view.
     * @param envelope - Envelope representing visible area of map.
     */
    void setCurrentExtent(Envelope envelope);

    /**
     * Add place to stops
     * @param p - Place
     */
    void addStop(Place p);

    /**
     * Set the travel mode
     * @param mode - String
     */
    void setTravelMode(String mode);

    /**
     * Return the travel mode
     * @return - TravelMode
     */
    TravelMode.TravelModeTypes getTravelMode();
  }
}
