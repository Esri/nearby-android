package com.esri.android.nearbyplaces.data;

import android.content.Context;
import android.support.annotation.NonNull;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Enables injection of production implementations for
 * {@link com.esri.android.nearbyplaces.data.PlacesServiceApi} at compile time.
 */
public class Injection {
  public static PlacesRepository providePlacesRepository(@NonNull Context context){
    checkNotNull(context);
    return PlacesRepositories.getInMemoryRepoInstance(new LocationService());
   // return PlacesRepositories.getInMemoryRepoInstance(new PlacesServiceApiImpl());
  }
}
