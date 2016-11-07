# Nearby Places
Explore the world around you.

## Description
Customize your exploration of places around you using [Esri’s World Geocoding Service](https://developers.arcgis.com/features/geocoding/) or your own [custom geocoding service](http://desktop.arcgis.com/en/arcmap/latest/manage-data/geocoding/the-geocoding-workflow.htm). The app uses Esri’s geocoding service to find hotels, restaurants or bars within a default radius of the device’s current location and provides routing directions to a selected place of interest. The geometry engine is used to sort the list of places based on distance and bearing from the device’s location.

The example application is open source and available on GitHub. You can modify it to display [categories](https://developers.arcgis.com/rest/geocode/api-reference/geocoding-category-filtering.htm) of places you care about or [configure](http://desktop.arcgis.com/en/arcmap/latest/manage-data/geocoding/creating-an-address-locator.htm) your own custom locator.

## Geocoding with Categories
The nearby-app uses a list of categories (e.g. “Hotel”, “Food”, “Pizza”) with the World Geocoding Service to display places matching these types near the current device location. These categories can be changed dynamically in the app using the filter widget. The service uses a hierarchical structure of categories allowing high level concepts, like a category level 1 descriptor (e.g. “POI” for place of interest) to be searched as well as more specific category level 3 types like “Brazilian Food” or “Science Museum” to be used. These category filters and other search criteria are defined using the SDK’s [Geocode Parameters](https://developers.arcgis.com/android/beta/api-reference/reference/com/esri/arcgisruntime/tasks/geocode/GeocodeParameters.html#setPreferredSearchLocation(com.esri.arcgisruntime.geometry.Point).

The geocode parameters are configured with the device's current location with the following code:
```
      GeocodeParameters parameters = new GeocodeParameters();
      parameters.setMaxResults(10);
      parameters.setPreferredSearchLocation(mCurrentLocation);
      List categories = parameters.getCategories();
      categories.add("Food");
      categories.add("Hotel");
      categories.add("Pizza");
      categories.add("Coffee Shop");
      categories.add("Bar or Pub");
      List outputAttributes = parameters.getResultAttributeNames();
      outputAttributes.add("*");
      ListenableFuture> results = mLocatorTask.geocodeAsync(searchText, parameters);
```
## Device Location
This app uses a [design pattern](https://developers.arcgis.com/android/guide/determine-your-app-map-pattern.htm#ESRI_SECTION1_58C46384E3484890A47629F8F12E6EF5) with a list of places and option to see the list on a map. The device location is obtained using Google’s Location Services API. In the future, leveraging the SDK’s can be used to obtain the device location outside of the MapView.

```
// Create an instance of GoogleAPIClient.
    if (mGoogleApiClient == null) {
      mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
          .addConnectionCallbacks(this)
          .addOnConnectionFailedListener(this)
          .addApi(LocationServices.API)
          .build();
    }

    @Override public void onConnected(@Nullable Bundle bundle) {
    	mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }
```

## Calculating Bearing and Distance
To determine distance and bearing, the [geometry engine](https://developers.arcgis.com/android/guide/geometry-operations.htm#GUID-D2E69684-B294-4725-87DA-3546BDDDFE0B) is used to calculate the [geodesic distance](https://geonet.esri.com/groups/coordinate-reference-systems/blog/2014/09/01/geodetic-distances-how-long-is-that-line-again) between the device location and each nearby point of interest. Measuring distance, determining spatial relationships, and altering geometries can be done locally on the mobile client.

The current device location requires a spatial reference when calculating distance and bearing.

```
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
## Licensing
Copyright 2016 Esri

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

A copy of the license is available in the repository's [license.txt](https://github.com/Esri/maps-app-android/blob/master/license.txt) file.

For information about licensing your deployed app, see [License your app](https://developers.arcgis.com/android/guide/license-your-app.htm).

[](Esri Tags: ArcGIS Android Mobile)
[](Esri Language: Java)​
