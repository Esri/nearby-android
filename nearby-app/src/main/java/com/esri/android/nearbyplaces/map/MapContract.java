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

import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult;

import java.util.List;

public interface MapContract {

  interface View extends BaseView<Presenter>{

    void showNearbyPlaces(List<Place> placeList);

    MapView getMapView();

    void centerOnPlace(Place p);

    void setRoute(RouteResult routeResult, Point start, Point end);

    void showMessage(String message);

    void showProgressIndicator(String message);

    void showRouteDetail(int position);

    void restoreMapView();

    void getRoute(LocationService service);

  }

  interface Presenter extends BasePresenter {

    void findPlacesNearby();

    void centerOnPlace(Place p);

    Place findPlaceForPoint(Point p);

    void getRoute();

    void setCurrentExtent(Envelope envelope);
  }
}
