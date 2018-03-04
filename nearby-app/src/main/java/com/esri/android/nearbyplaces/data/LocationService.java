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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.AngularUnit;
import com.esri.arcgisruntime.geometry.AngularUnitId;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeodeticCurveType;
import com.esri.arcgisruntime.geometry.GeodeticDistanceResult;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.LinearUnit;
import com.esri.arcgisruntime.geometry.LinearUnitId;
import com.esri.arcgisruntime.geometry.Multipoint;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteParameters;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask;
import com.esri.arcgisruntime.tasks.networkanalysis.Stop;
import com.esri.arcgisruntime.tasks.networkanalysis.TravelMode;

public class LocationService implements PlacesServiceApi {

  private final static String TAG = LocationService.class.getSimpleName();
  private static LocatorTask sLocatorTask = null;
  private static LocationService sInstance = null;
  private Point mCurrentLocation = null;
  private Envelope mCurrentEnvelope = null;
  private RouteTask mRouteTask = null;


  public static LocationService getInstance( ){
    if (sInstance == null){
      sInstance = new LocationService();
    }
    return sInstance;
  }
  // A local cache of found places
  private Map<String,Place> mCachedPlaces = null;


  public final static void configureService(@NonNull String locatorUrl, @NonNull final Runnable onSuccess, @NonNull final
      Runnable onError){
    if (null == sLocatorTask){
      sLocatorTask = new LocatorTask(locatorUrl);
      sLocatorTask.addDoneLoadingListener(new Runnable() {
        @Override public void run() {
          if (sLocatorTask.getLoadStatus() == LoadStatus.LOADED) {
            onSuccess.run();
          }else if (sLocatorTask.getLoadStatus() == LoadStatus.FAILED_TO_LOAD){
            Log.e("LocationService", "Locator task failed to load: " + sLocatorTask.getLoadError().getMessage());
            if (sLocatorTask.getLoadError().getCause() != null){
              Log.e("LocationService", "Locator task failed cause: " + sLocatorTask.getLoadError().getCause().getMessage());
            }
            onError.run();
          }
        }
      });
      sLocatorTask.loadAsync();
    }
  }

  @Override public void getRouteFromService(final Point start, final Point end, Context context,
      final RouteServiceCallback callback, List<Stop> stops, String travelMode) {
    mRouteTask = new RouteTask(context,"https://route.arcgis.com/arcgis/rest/services/World/Route/NAServer/Route_World/");
    mRouteTask.addDoneLoadingListener(new RouteSolver(mCurrentLocation,end, callback, stops, travelMode));
    mRouteTask.loadAsync();
  }

  @Override public void getPlacesFromService(@NonNull final GeocodeParameters parameters,@NonNull final PlacesServiceCallback callback)  {
    final String searchText = "";
    provisionOutputAttributes(parameters);
    provisionCategories(parameters);
    final ListenableFuture<List<GeocodeResult>> results = sLocatorTask
        .geocodeAsync(searchText, parameters);
    Log.i(TAG,"Geocode search started...");
    results.addDoneListener(new GeocodeProcessor(results, callback));
  }


  @Override public Place getPlaceDetail(final String placeName) {
    Place foundPlace = null;
    if (!mCachedPlaces.isEmpty()){
      if (mCachedPlaces.get(placeName) != null){
        foundPlace = mCachedPlaces.get(placeName);
      }
    }
    return foundPlace;
  }

  @Override public List<Place> getPlacesFromRepo() {
    return mCachedPlaces != null ? filterPlaces(new ArrayList<>(mCachedPlaces.values())) : null;
  }

  private static void provisionCategories(@NonNull final GeocodeParameters parameters){
    final List<String> categories = parameters.getCategories();
    categories.add("Food");
    categories.add("Hotel");
    categories.add("Pizza");
    categories.add("Coffee Shop");
    categories.add("Bar or Pub");
    categories.add("Trail");
    categories.add("Waterfall");
    categories.add("Winery");
    categories.add("Museum");
  }
  private static void provisionOutputAttributes(@NonNull final GeocodeParameters parameters){
    final List<String> outputAttributes = parameters.getResultAttributeNames();
    outputAttributes.clear();
    outputAttributes.add("*");
  }

  private static List<Place> filterPlaces(final List<Place> foundPlaces){
    final Collection<Place> placesToRemove = new ArrayList<>();
    final CategoryKeeper keeper = CategoryKeeper.getInstance();
    final List<String> selectedTypes = keeper.getSelectedTypes();
    if (!selectedTypes.isEmpty()){
      for (final Place p: foundPlaces) {
        for (final String filter : selectedTypes){
          if (filter.equalsIgnoreCase(p.getType())){
            placesToRemove.add(p);
          }
        }
      }
    }
    if (!placesToRemove.isEmpty()){
      foundPlaces.removeAll(placesToRemove);
    }
    //Log.i("FilteredPlaces", "After filtering on categories, there are " + foundPlaces.size());
    return foundPlaces;
  }

  public void setCurrentLocation(final Location currentLocation) {
    mCurrentLocation =  new Point(currentLocation.getLongitude(), currentLocation.getLatitude());
  }

  public void setCurrentEnvelope(final Envelope envelope){
    mCurrentEnvelope = envelope;
  }
  public Point getCurrentLocation(){
    return mCurrentLocation;
  }

  public Envelope getCurrentEnvelope(){
    return mCurrentEnvelope;
  }

  private class GeocodeProcessor implements Runnable{
    private final ListenableFuture<List<GeocodeResult>> mResults;
    private final PlacesServiceCallback mCallback;
    public GeocodeProcessor(final ListenableFuture<List<GeocodeResult>> results,final PlacesServiceCallback callback ){
      mCallback = callback;
      mResults = results;
    }
    @Override public void run() {

      try {
        if (mCachedPlaces == null){
          mCachedPlaces = new HashMap<>();
        }
        mCachedPlaces.clear();
        final List<GeocodeResult> data = mResults.get();
        final List<Place> places = new ArrayList<Place>();
        int i = 0;
        for (final GeocodeResult r: data){
          i = i + 1;
          final Map<String,Object> attributes = r.getAttributes();
          final String address = (String) attributes.get("Place_addr");
          final String name = (String) attributes.get("PlaceName");
          final String phone = (String) attributes.get("Phone");
          final String url = (String) attributes.get("URL");
          final String type = (String) attributes.get("Type");
          final Point location = r.getDisplayLocation();

          final Place place = new Place(name, type, location, address, url,phone,null,0);
          setBearingAndDistanceForPlace(place);
          Log.i("PLACE", place.toString());

          // Envelope is null the very first time the geocode search runs
          // because the search is initiated from the list view which
          // has no extent.
          if (mCurrentEnvelope != null){
            mCurrentEnvelope = (Envelope) GeometryEngine.project(mCurrentEnvelope, SpatialReferences.getWgs84());

            if (location != null){
              // Filter out any places not within the current envelope
              final Point placePoint = new Point(location.getX(),location.getY(), SpatialReferences.getWgs84());
              if (GeometryEngine.within(placePoint,mCurrentEnvelope)){
                places.add(place);
                mCachedPlaces.put(name,place);
              }else{
                Log.i("GeometryEngine", "***Excluding " + place.getName() + " because it's outside the visible area of map.***");
              }
            }

          }else{
            places.add(place);
            mCachedPlaces.put(name, place);
          }
        }

        mCallback.onLoaded(filterPlaces(places));
      } catch (final Exception e) {
        Log.e("LocationService", "Geocoding processing problem " + e.getMessage());
      }
    }
  }
  private final void setBearingAndDistanceForPlace(final Place place){
    String bearing =  null;
    if ((mCurrentLocation != null) && (place.getLocation() != null)){
      final LinearUnit linearUnit = new LinearUnit(LinearUnitId.METERS);
      final AngularUnit angularUnit = new AngularUnit(AngularUnitId.DEGREES);
      final Point newPoint = new Point(mCurrentLocation.getX(), mCurrentLocation.getY(), place.getLocation().getSpatialReference() );
      final GeodeticDistanceResult result =GeometryEngine.distanceGeodetic(newPoint, place.getLocation(),linearUnit, angularUnit, GeodeticCurveType.GEODESIC);
      final double distance = result.getDistance();
      place.setDistance(Math.round(distance));
      final double degrees = result.getAzimuth1();
      if ((degrees > -22.5) && (degrees <= 22.5)){
        bearing = "N";
      }else if ((degrees > 22.5) && (degrees <= 67.5)){
        bearing = "NE";
      }else if ((degrees > 67.5) && (degrees <= 112.5)){
        bearing = "E";
      }else if ((degrees > 112.5) && (degrees <= 157.5)){
        bearing = "SE";
      }else if( degrees > 157.5 || degrees <= -157.5){
        bearing = "S";
      }else if ((degrees > -157.5) && (degrees <= -112.5)){
        bearing = "SW";
      }else if ((degrees > -112.5) && (degrees <= -67.5)){
        bearing = "W";
      }else if ((degrees > -67.5) && (degrees <= -22.5)){
        bearing = "NW";
      }
      if (bearing==null){
        Log.i(TAG, "Can't find bearing for " + degrees);
      }

    }
    place.setBearing(bearing);
  }

  /**
   * Return the current extent.  If the current extent is null,
   * calculate extent based on current search results.
   * @return Envelope
   */
  public final Envelope getResultEnvelope(){
    if (mCurrentEnvelope == null){
      Envelope envelope = null;
      final List<Place> places = getPlacesFromRepo();
      if (!places.isEmpty()){
        final List<Point> points = new ArrayList<>();
        for (final Place place : places){
          points.add(place.getLocation());
        }
        final Multipoint mp = new Multipoint(points);
        envelope = GeometryEngine.buffer(mp, 0.0007).getExtent();
      }
      return envelope;
    }else{
      return mCurrentEnvelope;
    }

  }
  /**
   * A helper class for solving routes
   */
  private class RouteSolver implements Runnable{
    private final Stop origin;
    private final RouteServiceCallback mCallback;
    private final Stop destination;
    private final List<Stop> mStops;
    private final String mTravelMode;

    public RouteSolver(final Point start, final Point end, final RouteServiceCallback callback,
                       List<Stop> stops, String mode ){
      origin = new Stop(start);
      destination = new Stop(end);
      mCallback = callback;
      mStops = stops;
      mTravelMode = mode;
    }
    @Override
    public void run (){
      final LoadStatus status = mRouteTask.getLoadStatus();

      // Has the route task loaded successfully?
      if (status == LoadStatus.FAILED_TO_LOAD) {
        Log.i(TAG, mRouteTask.getLoadError().getMessage());
        Log.i(TAG, "CAUSE = " + mRouteTask.getLoadError().getCause().getMessage());
      } else {
        final ListenableFuture<RouteParameters> routeTaskFuture = mRouteTask
            .createDefaultParametersAsync();
        // Add a done listener that uses the returned route parameters
        // to build up a specific request for the route we need
        routeTaskFuture.addDoneListener(new Runnable() {

          @Override
          public void run() {
            try {
              final RouteParameters routeParameters = routeTaskFuture.get();
              TravelMode mode = routeParameters.getTravelMode();
              configureTravelMode(mode, mTravelMode);
              routeParameters.setTravelMode(mode);
              configureStops(routeParameters, mStops);

              // We want the task to return directions and routes
              routeParameters.setReturnDirections(true);

              routeParameters.setOutputSpatialReference(SpatialReferences.getWebMercator());

              final ListenableFuture<RouteResult> routeResFuture = mRouteTask
                  .solveRouteAsync(routeParameters);
              routeResFuture.addDoneListener(new Runnable() {
                @Override
                public void run() {
                  try {
                    final RouteResult routeResult = routeResFuture.get();
                    // Show route results
                    if (routeResult != null){
                      mCallback.onRouteReturned(routeResult);

                    }else{
                      Log.i(TAG, "NO RESULT FROM ROUTING");
                    }

                  } catch (final Exception e) {
                    Log.e(TAG, e.getMessage());
                  }
                }
              });
            } catch (final Exception e1){
              Log.e(TAG,e1.getMessage() );
            }
          }
        });
      }
    }

    /**
     * Configure travel mode based on mode string
     * @param mode - TravelMode
     * @param modeString - String
     */
    private void configureTravelMode(TravelMode mode, String modeString) {
      if (modeString.equalsIgnoreCase("Walk")) {
        mode.setName("Walking Time");
        mode.setImpedanceAttributeName("WalkTime");
        mode.setTimeAttributeName("WalkTime");
        // Set the restriction attributes for walk times
        List<String> restrictionAttributes = mode.getRestrictionAttributeNames();
        // clear default restrictions
        restrictionAttributes.clear();
        // add pedestrian restrictions
        restrictionAttributes.add("Avoid Roads Unsuitable for Pedestrians");
        restrictionAttributes.add("Preferred for Pedestrians");
        restrictionAttributes.add("Walking");

        for (String s : restrictionAttributes){
          Log.i(TAG, "Restriciton = " + s);
        }
        Log.i(TAG, "Travel mode = " + mode.getName());
      } else {
        mode.setName("Driving Time");
        mode.setType("AUTOMOBILE");
        mode.setImpedanceAttributeName("TravelTime");
        mode.setTimeAttributeName("TravelTime");
        // Set the restriction attributes for walk times
        List<String> restrictionAttributes = mode.getRestrictionAttributeNames();
        // clear default restrictions
        restrictionAttributes.clear();
        // add pedestrian restrictions
        restrictionAttributes.add("Avoid Private Roads");
        restrictionAttributes.add("Driving an Automobile");
        restrictionAttributes.add("Through Traffic Prohibited");
        restrictionAttributes.add("Roads Under Construction Prohibited");
        restrictionAttributes.add("Avoid Gates");
        restrictionAttributes.add("Avoid Express Lanes");
        restrictionAttributes.add("Avoid Carpool Roads");


        for (String s : restrictionAttributes){
          Log.i(TAG, "Restriciton = " + s);
        }
        Log.i(TAG, "Travel mode = " + mode.getName());
      }
    }

    /**
     * Configure the route stops
     * @param parameters - RouteParameters
     * @param stops - List of Stop items
     */
    private void configureStops(RouteParameters parameters, List<Stop> stops) {
      // Add the origin
      parameters.getStops().add(origin);

      if (mStops.size() > 0) {
        for (Stop stop : mStops) {
          parameters.getStops().add(stop);
        }
        parameters.setFindBestSequence(true);
        parameters.setPreserveFirstStop(true);
        parameters.setPreserveLastStop(true);
        Log.i(TAG, "Stops added");
      }
      parameters.getStops().add(destination);
      Log.i(TAG, "Total stops =" + parameters.getStops().size());
    }
  }

}
