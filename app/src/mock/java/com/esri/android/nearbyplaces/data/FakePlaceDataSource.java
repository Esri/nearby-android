package com.esri.android.nearbyplaces.data;

import android.support.annotation.NonNull;
import com.esri.android.nearbyplaces.data.PlacesRepository.LoadPlacesCallback;
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
    return new Place("Powell's Books", "bookstore", new Point(45.521658, -122.7035132), "1055 W Burnside Portland, OR 97209",null, "(503) 228-4651", "NE",null);
  }
}
