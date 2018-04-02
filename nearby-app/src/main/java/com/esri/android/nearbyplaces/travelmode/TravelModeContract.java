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

package com.esri.android.nearbyplaces.travelmode;

import com.esri.android.nearbyplaces.BasePresenter;
import com.esri.android.nearbyplaces.BaseView;
import com.esri.android.nearbyplaces.data.TravelMode;

/**
 * This is the contract between the Presenter and View components of the MVP pattern.
 * It defines methods and logic used when showing the list of travel modes.
 */
public interface TravelModeContract {
  interface View extends BaseView<Presenter> {
  }
  interface Presenter extends BasePresenter {
    /**
     * Set the travel mode
     * @param travelMode - String
     */
    void setTravelMode(String travelMode);

    /**
     * Get the travel mode
     * @return - String
     */
    String getTravelMode();
  }

  interface TravelModeView {
    /**
     * Pass the chosen travel mode when dialog is closed
     * @param mode - TravelMode
     */
    void onTravelModeClose(TravelMode.TravelModeTypes mode);
  }
}
