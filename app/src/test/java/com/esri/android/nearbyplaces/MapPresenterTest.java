package com.esri.android.nearbyplaces;

import com.esri.android.nearbyplaces.data.Place;
import com.esri.android.nearbyplaces.data.PlacesRepository;
import com.esri.android.nearbyplaces.map.MapContract;
import com.esri.android.nearbyplaces.map.MapPresenter;
import com.esri.android.nearbyplaces.mapplace.MapPlaceContract;
import com.esri.android.nearbyplaces.mapplace.MapPlaceMediator;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.verify;

import java.util.List;

/**
 * Created by sand8529 on 7/11/16.
 */
public class MapPresenterTest {

  MapPresenter mMapPresenter;

  private static List<Place> PLACES;

  @Mock
  private MapContract.View mMapView;

  @Mock
  private MapPlaceContract mapPlaceContract;

  @Captor
  private ArgumentCaptor<PlacesRepository.GetPlaceCallback> mPlacesServiceCallbackCaptor;

  @Before
  public void setUpMapPresenter(){
    // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
    // inject the mocks in the test the initMocks method needs to be called.
    MockitoAnnotations.initMocks(this);
    mapPlaceContract = new MapPlaceMediator();
    mMapPresenter = new MapPresenter(mMapView);

    PLACES = Lists.newArrayList(
        new Place("Powells Books",null,null,null,null,null,null),
        new Place("Stumptown Coffee", null,null,null,null,null,null),
        new Place("Mt. Hood", null,null,null,null,null,null)
    );
  }
  @Test
  public void findPlacesNearby(){
    mMapPresenter.start();
    verify(mMapView).getLocationDisplay();
    mMapPresenter.findPlacesNearby();

  }
}
