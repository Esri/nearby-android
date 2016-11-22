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

import android.app.Activity;
import android.app.AlertDialog;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.*;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorInfo;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.esri.arcgisruntime.tasks.route.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class LocationService implements PlacesServiceApi {

  private final static String TAG = LocationService.class.getSimpleName();
  private static LocatorTask mLocatorTask;
  private static LocationService instance = null;
  private Point mCurrentLocation;
  private RouteTask mRouteTask;


  public static LocationService getInstance( ){
    if (instance == null){
      instance = new LocationService();
    }
    return instance;
  }

  private final Map<String,Place> mappedPlaces = new HashMap<>();


  public static void configureService(@NonNull String locatorUrl, @NonNull Runnable doneListener){
    checkNotNull(locatorUrl);
    checkNotNull(doneListener);
    if (null == mLocatorTask){
      mLocatorTask = new LocatorTask(locatorUrl);
      mLocatorTask.addDoneLoadingListener(doneListener);
      mLocatorTask.loadAsync();
    }
  }

  @Override public void getRouteFromService(Point start, Point end, RouteServiceCallback callback) {
    if (mRouteTask == null){
      mRouteTask = new RouteTask("https://route.arcgis.com/arcgis/rest/services/World/Route/NAServer/Route_World/");
    }
    mRouteTask.addDoneLoadingListener(new RouteSolver(mCurrentLocation,end, callback));
    mRouteTask.loadAsync();
  }

  @Override public void getPlacesFromService(@NonNull GeocodeParameters parameters,@NonNull final PlacesServiceCallback callback)  {
    checkNotNull(parameters);
    checkNotNull(callback);
    String searchText = "";
    provisionOutputAttributes(parameters);
    provisionCategories(parameters);
    final ListenableFuture<List<GeocodeResult>> results = mLocatorTask
        .geocodeAsync(searchText, parameters);
    Log.i(TAG,"Geocode search started...");
    results.addDoneListener(new GeocodeProcessor(results, callback));
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
    return filterPlaces(new ArrayList<>(mappedPlaces.values()));
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

  private List<Place> filterPlaces(List<Place> foundPlaces){
    List<Place> placesToRemove = new ArrayList<>();
    CategoryKeeper keeper = CategoryKeeper.getInstance();
    List<String> selectedTypes = keeper.getSelectedTypes();
    if (selectedTypes.isEmpty()){
      return foundPlaces;
    }else{
      for (Place p: foundPlaces) {
        for (String filter : selectedTypes){
          if (filter.equalsIgnoreCase(p.getType())){
            placesToRemove.add(p);
          }
        }
      }
    }
    if (!placesToRemove.isEmpty()){
      foundPlaces.removeAll(placesToRemove);
    }
    return foundPlaces;
  }

  public void setCurrentLocation(Location currentLocation) {
    mCurrentLocation =  new Point(currentLocation.getLongitude(), currentLocation.getLatitude());
  }
  public Point getCurrentLocation(){
    return mCurrentLocation;
  }

  private class GeocodeProcessor implements Runnable{
    private final ListenableFuture<List<GeocodeResult>> mResults;
    private final PlacesServiceCallback mCallback;
    public GeocodeProcessor(ListenableFuture<List<GeocodeResult>> results,final PlacesServiceCallback callback ){
      mCallback = callback;
      mResults = results;
    }
    @Override public void run() {

      try {
        mappedPlaces.clear();
        List<GeocodeResult> data = mResults.get();
        List<Place> places = new ArrayList<Place>();
        for (GeocodeResult r: data){
          Map<String,Object> attributes = r.getAttributes();
          String address = (String) attributes.get("Place_addr");
          String name = (String) attributes.get("PlaceName");
          String phone = (String) attributes.get("Phone");
          String url = (String) attributes.get("URL");
          String type = (String) attributes.get("Type");
          Point point = r.getDisplayLocation();

          Place place = new Place(name, type, point, address, url,phone,null,0);
          setBearingAndDistanceForPlace(place);
          Log.i("PLACE", place.toString());
          places.add(place);
          mappedPlaces.put(name,place);

        }
        mCallback.onLoaded(filterPlaces(places));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  private void setBearingAndDistanceForPlace(Place place){
    String bearing =  null;
    if (mCurrentLocation != null && place.getLocation() != null){
      LinearUnit linearUnit = new LinearUnit(LinearUnitId.METERS);
      AngularUnit angularUnit = new AngularUnit(AngularUnitId.DEGREES);
      Point newPoint = new Point(mCurrentLocation.getX(), mCurrentLocation.getY(), place.getLocation().getSpatialReference() );
      GeodesicDistanceResult result =GeometryEngine.distanceGeodesic(newPoint, place.getLocation(),linearUnit, angularUnit, GeodeticCurveType.GEODESIC);
      double distance = result.getDistance();
      place.setDistance(Math.round(distance));
      double degrees = result.getAzimuth1();
      if (degrees > -22.5  && degrees <= 22.5){
        bearing = "N";
      }else if (degrees > 22.5 && degrees <= 67.5){
        bearing = "NE";
      }else if (degrees > 67.5 && degrees <= 112.5){
        bearing = "E";
      }else if (degrees > 112.5 && degrees <= 157.5){
        bearing = "SE";
      }else if( (degrees > 157.5 ) || (degrees <= -157.5)){
        bearing = "S";
      }else if (degrees > -157.5 && degrees <= -112.5){
        bearing = "SW";
      }else if (degrees > -112.5 && degrees <= -67.5){
        bearing = "W";
      }else if (degrees > -67.5 && degrees <= -22.5){
        bearing = "NW";
      }
      if (bearing==null){
        Log.i(TAG, "Can't find bearing for " + degrees);
      }

    }
    place.setBearing(bearing);
  }

  /**
   * Return the extent of the search results with a small buffer
   * @return Envelope
   */
  public Envelope getResultEnveope(){
    Envelope envelope = null;
    List<Place> places = getPlacesFromRepo();
    if (!places.isEmpty()){
      List<Point> points = new ArrayList<>();
      for (Place place : places){
        points.add(place.getLocation());
      }
      Multipoint mp = new Multipoint(points);
      envelope = GeometryEngine.buffer(mp, 0.0007).getExtent();
    }
    return envelope;
  }
  /**
   * A helper class for solving routes
   */
  private class RouteSolver implements Runnable{
    private final Stop origin;
    private final RouteServiceCallback mCallback;
    private final Stop destination;

    public RouteSolver(Point start, Point end,RouteServiceCallback callback ){
      origin = new Stop(start);
      destination = new Stop(end);
      mCallback = callback;
    }
    @Override
    public void run (){
      LoadStatus status = mRouteTask.getLoadStatus();

      // Has the route task loaded successfully?
      if (status == LoadStatus.FAILED_TO_LOAD) {
        Log.i(TAG, mRouteTask.getLoadError().getMessage());
        Log.i(TAG, "CAUSE = " + mRouteTask.getLoadError().getCause().getMessage());
      } else {
        final ListenableFuture<RouteParameters> routeTaskFuture = mRouteTask
            .generateDefaultParametersAsync();
        // Add a done listener that uses the returned route parameters
        // to build up a specific request for the route we need
        routeTaskFuture.addDoneListener(new Runnable() {

          @Override
          public void run() {
            try {
              final RouteParameters routeParameters = routeTaskFuture.get();
              final TravelMode mode = routeParameters.getTravelMode();
              mode.setType("WALK");
              mode.setName("Walking Time");
              // Add a stop for origin and destination
              routeParameters.setTravelMode(mode);
              routeParameters.getStops().add(origin);
              routeParameters.getStops().add(destination);
              // We want the task to return driving directions and routes
              routeParameters.setReturnDirections(true);
              routeParameters.setDirectionsDistanceTextUnits(
                  DirectionDistanceTextUnits.IMPERIAL);
              routeParameters.setOutputSpatialReference(SpatialReferences.getWebMercator());

              final ListenableFuture<RouteResult> routeResFuture = mRouteTask
                  .solveAsync(routeParameters);
              routeResFuture.addDoneListener(new Runnable() {
                @Override
                public void run() {
                  try {
                    RouteResult routeResult = routeResFuture.get();
                    Log.i(TAG, routeParameters.getTravelMode().getName());
                    // Show route results
                    if (routeResult != null){
                      mCallback.onRouteReturned(routeResult);

                    }else{
                      Log.i(TAG, "NO RESULT FROM ROUTING");
                    }

                  } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                  }
                }
              });
            } catch (Exception e1){
              Log.e(TAG,e1.getMessage() );
            }
          }
        });
      }
    }
  }

}
