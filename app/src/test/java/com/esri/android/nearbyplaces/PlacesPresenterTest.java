package com.esri.android.nearbyplaces;

import com.esri.android.nearbyplaces.data.Place;
import com.esri.android.nearbyplaces.data.PlacesRepository;
import com.esri.android.nearbyplaces.mapplace.MapPlaceContract;
import com.esri.android.nearbyplaces.mapplace.MapPlaceMediator;
import com.esri.android.nearbyplaces.places.PlacesContract;
import com.esri.android.nearbyplaces.places.PlacesPresenter;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class PlacesPresenterTest {

  PlacesPresenter mPlacesPresenter;

  private static List<Place> PLACES;

  @Mock
  private PlacesContract.View mPlacesView;

  @Mock
  private MapPlaceContract mapPlaceContract;
  /**
   * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
   * perform further actions or assertions on them.
   */
  @Captor
  private ArgumentCaptor<PlacesRepository.LoadPlacesCallback> mPlacesServiceCallbackCaptor;

  @Before
  public void setupTasksPresenter() {
    // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
    // inject the mocks in the test the initMocks method needs to be called.
    MockitoAnnotations.initMocks(this);
    mapPlaceContract = new MapPlaceMediator();
    mPlacesPresenter = new PlacesPresenter( mPlacesView);

    // The presenter won't update the view unless it's active.

    when(mPlacesView.isActive()).thenReturn(true);

    PLACES = Lists.newArrayList(
        new Place("Powells Books",null,null,null,null,null,null),
        new Place("Stumptown Coffee", null,null,null,null,null,null),
        new Place("Mt. Hood", null,null,null,null,null,null)
    );
  }
  @Test
  public void loadPlacesIntoView(){

    // When view is first shown, load list of places
    mPlacesPresenter.setPlacesNearby(PLACES);

    // While loading places, a progress indicator is shown
    verify(mPlacesView).showNearbyPlaces(PLACES);

    // Callback is captured and invoked with stubbed places (PLACES)
    /*verify(mPlacesDataSource).getPlacesFromService( mPlacesServiceCallbackCaptor.capture());
    mPlacesServiceCallbackCaptor.getValue().onPlacesLoaded(PLACES);

    // Then progress indicator is hidden and all places are shown in UI
    verify(mPlacesView).showProgressIndicator(false);
    ArgumentCaptor<List> showPlacesListArgumentCaptor = ArgumentCaptor.forClass(List.class);
    verify(mPlacesView).showPlaces(showPlacesListArgumentCaptor.capture());
    assertTrue(showPlacesListArgumentCaptor.getValue().size() == 3);
  }

  @Test
  public void loadPlaceDetail(){
    // Make up a stubbed place, exclude the Point since mockito can't deal with final classes.
    Place place = new Place("Powell's Books", "bookstore", null, "1055 W Burnside Portland, OR 97209",null, "(503) 228-4651", "NE");

    // Open the place detail
    mPlacesPresenter.loadPlaceDetail(place.getName());

    // Then verify that UI would show detailed place
    verify(mPlacesView).showPlaceDetail(any(Place.class));*/
  }
}