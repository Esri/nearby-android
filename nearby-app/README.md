# Nearby Places
Explore the world around you.

## Description
Customize your exploration of places around you using [Esri’s World Geocoding Service](https://developers.arcgis.com/features/geocoding/) or your own [custom geocoding service](http://desktop.arcgis.com/en/arcmap/latest/manage-data/geocoding/the-geocoding-workflow.htm). The app uses Esri’s geocoding service to find hotels, restaurants or bars within a default radius of the device’s current location and provides routing directions to a selected place of interest. The ArcGIS Android Geometry Engine API is used to sort the list of places based on distance and bearing from the device’s location.

The example application is open source and available on GitHub. You can modify it to display [categories](https://developers.arcgis.com/rest/geocode/api-reference/geocoding-category-filtering.htm) of places you care about or [configure](http://desktop.arcgis.com/en/arcmap/latest/manage-data/geocoding/creating-an-address-locator.htm) your own custom locator.

## Geocoding with Categories
The nearby-app uses category filters (e.g. “Hotel”, “Food”, “Pizza”) with the World Geocoding Service to display places matching these types near the current device location. The geocoding service uses a hierarchical structure of categories allowing high level concepts, like a category level 1 descriptor (e.g. “POI” for place of interest) to be searched as well as more specific category level 3 types like “Brazilian Food” or “Science Museum” to be used. The category filters and other search criteria are defined using the SDK’s [Geocode Parameters](https://developers.arcgis.com/android/latest/api-reference/reference/com/esri/arcgisruntime/tasks/geocode/GeocodeParameters.html).

The geocode parameters are configured with the maximum number of results to return, the desired categories, the device's current location, and [output fields](https://developers.arcgis.com/rest/geocode/api-reference/geocoding-service-output.htm#ESRI_SECTION1_42D7D3D0231241E9B656C01438209440) using the following pattern.  Note that the geocoding service supports a specific list of categories defined [here](https://developers.arcgis.com/rest/geocode/api-reference/geocoding-category-filtering.htm#ESRI_SECTION1_502B3FE2028145D7B189C25B1A00E17B).

```java
GeocodeParameters parameters = new GeocodeParameters();

// We're interested in the top ten nearest places
parameters.setMaxResults(10);

// This limits the search to a radius of 50 kilometers
// around the current location.
parameters.setPreferredSearchLocation(mCurrentLocation);

// Retrieve a mutable list
List categories = parameters.getCategories();

// Add a subset of specific keyword categories
// known to the service. See reference above.
categories.add("Food");
categories.add("Hotel");
categories.add("Pizza");
categories.add("Coffee Shop");
categories.add("Bar or Pub");

List outputAttributes = parameters.getResultAttributeNames();
// Return all of the service output fields
outputAttributes.add("*");

// Execute the search[]
<ListenableFuture> results = mLocatorTask.geocodeAsync(searchText, parameters);
```

## Device Location
This app uses a [mapless app pattern](https://developers.arcgis.com/android/guide/determine-your-app-map-pattern.htm#ESRI_SECTION1_58C46384E3484890A47629F8F12E6EF5) with a list of places and option to see the list on a map. Since the app starts with a list, rather than a map, the device location is obtained using Google’s Location Services API. In the future, the Runtime SDK can be used to obtain the device location outside of the MapView.

```java
// Google's location services are configured in the
// PlacesFragment onCreate method.
if (mGoogleApiClient == null) {
    // Create an instance of GoogleAPIClient.
    mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
      .addConnectionCallbacks(this)
      .addOnConnectionFailedListener(this)
      .addApi(LocationServices.API)
      .build();
}
// Once Google's location service is connected,
// you can use the device location to start the 
// geocoding search.
@Override public void onConnected(@Nullable Bundle bundle) {
    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    startSearch(mLastLocation);
}
```

## Calculating Bearing and Distance
To determine distance and bearing, the [geometry engine](https://developers.arcgis.com/android/latest/api-reference/reference/com/esri/arcgisruntime/geometry/GeometryEngine.html) is used to calculate the [geodesic distance](https://geonet.esri.com/groups/coordinate-reference-systems/blog/2014/09/01/geodetic-distances-how-long-is-that-line-again) between the device location and each nearby point of interest. Measuring distance, determining spatial relationships, and altering geometries can be done locally on the mobile client.

The current device location requires a spatial reference when calculating distance and bearing.

```java
LinearUnit linearUnit = new LinearUnit(LinearUnitId.METERS);
AngularUnit angularUnit = new AngularUnit(AngularUnitId.DEGREES);

//The current location is obtained from the Google Location API 
//needs to be created as a new point with a spatial reference.
Point newPoint = new Point(mCurrentLocation.getX(), mCurrentLocation.getY(), place.getLocation().getSpatialReference() );
GeodesicDistanceResult result =GeometryEngine.distanceGeodesic(newPoint, place.getLocation(),linearUnit, angularUnit, GeodeticCurveType.GEODESIC);
double distance = result.getDistance();
place.setDistance(Math.round(distance));

// Bearing degrees are returned in a range between -180 to 180.
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
```

## Mutable Lists
A common pattern throughout the SDK is the use of mutable lists to control a variety of settings.  Examples of this occur in the following places across the nearby app:

```java

// Setting GeocodeParameters (see the section above about Geocoding With Categories)

// Setting the restriction attributes for walk times
// when solving for a route.

List<String> restrictionAttributes = mode.getRestrictionAttributeNames();
restrictionAttributes.clear();
restrictionAttributes.add("Avoid Roads Unsuitable for Pedestrians");
restrictionAttributes.add("Preferred for Pedestrians");
restrictionAttributes.add("Walking");

// Showing the route result, the route overlay is added
// only once.  Subsequent access is via "getGraphcis"

if (mRouteOverlay == null) {
    mRouteOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(mRouteOverlay);
}else{
    // Clear any previous route
    mRouteOverlay.getGraphics().clear();
}

//Adding graphics to the map
final BitmapDrawable pin = (BitmapDrawable) ContextCompat.getDrawable(getActivity(),getDrawableForPlace(place)) ;
final PictureMarkerSymbol pinSymbol = new PictureMarkerSymbol(pin);
final Point graphicPoint = place.getLocation();
final Graphic graphic = new Graphic(graphicPoint, pinSymbol);
mGraphicOverlay.getGraphics().add(graphic);
```

## Two Workarounds

### Viewpoint and Location Display
In version 100.00 of the Android SDK there's a bug when setting the viewpoint of the map view and turning on location display.  For example, the following code will not zoom to the given viewpoint.  Instead, the map extent remains unchanged when displaying device location.

```
mMapView = (MapView) root.findViewById(R.id.map);
final ArcGISMap map = new ArcGISMap(Basemap.createNavigationVector());
mMapView.setMap(map);

// Setting the viewpoint and then
// calling getLocationDisplay will cause
// the map view to ignore the given viewpoint.

mMapView.setViewpoint(mViewpoint); // a non-null viewpoint
mLocationDisplay = mMapView.getLocationDisplay();
mLocationDisplay.startAsync();

```
The desired behavior is to have the map view change the visible area of the map view to the given viewpoint and display the device location.  This is accomplished by 

  1.  Setting the viewpoint
  2.  Waiting for the map's draw status to be complete
  3.  Get the location display and start it asynchronously

```
mMapView = (MapView) root.findViewById(R.id.map);
final ArcGISMap map = new ArcGISMap(Basemap.createNavigationVector());
mMapView.setMap(map);

// Set view point first
mMapView.setViewpoint(mViewpoint);

// Wait for draw status to be complete before getting and 
// starting location display.
mMapView.addDrawStatusChangedListener(new DrawStatusChangedListener() {
   @Override public void drawStatusChanged(final DrawStatusChangedEvent drawStatusChangedEvent) {
        if (drawStatusChangedEvent.getDrawStatus() == DrawStatus.COMPLETED){
          mLocationDisplay = mMapView.getLocationDisplay();
          mLocationDisplay.startAsync();
        }
    }
});
```

### Handling Fling Gestures in the MapView
In version 100.00 of the Android SDK there's a workaround to correctly manage fling gestures on the map.  Navigation changes are monitored by attaching a NavigationChangedListener to the MapView.  With each event received, we check the MapView.isNavigating() property. Such logic works well for discrete gestures. If a fling gesture is being executed, there's a slight pause before the fling that will result in the map view returning false for isNavigating().  To account for this delay, we add logic to the message queue and execute after 50 milliseconds.

```
    mNavigationChangedListener = new NavigationChangedListener() {
      // This is a workaround for detecting when a fling motion has completed on the map view. The
      // NavigationChangedListener listens for navigation changes, not whether navigation has completed.  We wait
      // a small interval before checking if map is view still navigating.
      
      @Override public void navigationChanged(final NavigationChangedEvent navigationChangedEvent) {
       if (!mMapView.isNavigating()){
         Handler handler = new Handler();
         
         // Add a 50 ms delay and check again if map view is navigating.
         handler.postDelayed(new Runnable() {
           @Override public void run() {
             if (!mMapView.isNavigating()) {
               onMapViewChange();
             }
           }
         }, 50);
       }
      }

    };
    mMapView.addNavigationChangedListener(mNavigationChangedListener);
```
