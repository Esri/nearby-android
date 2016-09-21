package com.esri.android.nearbyplaces;
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

import com.esri.android.nearbyplaces.data.Place;
import com.esri.android.nearbyplaces.data.PlacesRepository;
import com.esri.android.nearbyplaces.map.MapContract;
import com.esri.android.nearbyplaces.map.MapPresenter;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;


public class MapPresenterTest {

  MapPresenter mMapPresenter;

  private static List<Place> PLACES;

  @Mock
  private MapContract.View mMapView;


  @Captor
  private ArgumentCaptor<PlacesRepository.GetPlaceCallback> mPlacesServiceCallbackCaptor;

  @Before
  public void setUpMapPresenter(){
    // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
    // inject the mocks in the test the initMocks method needs to be called.
    MockitoAnnotations.initMocks(this);
    mMapPresenter = new MapPresenter(mMapView);

    PLACES = Lists.newArrayList(
        new Place("Powells Books",null,null,null,null,null,null,0),
        new Place("Stumptown Coffee", null,null,null,null,null,null,0),
        new Place("Mt. Hood", null,null,null,null,null,null,0)
    );
  }
  @Test
  public void findPlacesNearby(){
    mMapPresenter.start();
  //  verify(mMapView).getLocationDisplay();
    mMapPresenter.findPlacesNearby();

  }
}
