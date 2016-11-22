package com.esri.android.nearbyplaces;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import com.esri.android.nearbyplaces.data.Place;
import com.esri.android.nearbyplaces.data.PlacesServiceApi;
import com.esri.android.nearbyplaces.places.PlacesActivity;
import org.junit.runner.RunWith;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests for the main screen showing a list of nearby places
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class PlacesScreenTest {

  /**
   * {@link ActivityTestRule} is a JUnit {@link Rule @Rule} to launch your activity under test.
   * <p>
   * Rules are interceptors which are executed for each test method and are important building
   * blocks of Junit tests.
   */
  @Rule
  public ActivityTestRule<PlacesActivity> mPlacesActivityTestRule =
      new ActivityTestRule<PlacesActivity>(PlacesActivity.class) {

        /**
         * To avoid a long list of places and the need to scroll through the list to find a
         * place, we call {@link PlacesServiceApi#deleteAllPlaces()} before each test.
         */
        @Override
        protected void beforeActivityLaunched() {
          super.beforeActivityLaunched();
          // Doing this in @Before generates a race condition.
          //Injection.providePlacesRepository(InstrumentationRegistry.getTargetContext()).deleteAllPlaces();
        }
      };

  @Test
  public void showAllPlaces(){
    // Add some places
    Place place1 = createPlace("Powells");


  }
  private Place createPlace(String placeName){
    return new Place(placeName,null,null,null,null,null,null,0);
  }
}
