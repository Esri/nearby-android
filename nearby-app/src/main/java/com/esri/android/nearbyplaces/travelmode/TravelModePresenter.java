/* Copyright 2018 Esri
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

/**
 * This is the concrete implementation of the Presenter defined in the TravelModeContract.
 * It encapsulates business logic and drives the behavior of the View.
 */
public class TravelModePresenter implements TravelModeContract.Presenter {
  private TravelModeContract.View mView;
  private String mTravelMode;

  @Override public void start() {}

  @Override
  public void setTravelMode(String travelMode) {
    mTravelMode = travelMode;
  }

  @Override
  public String getTravelMode() {
    return mTravelMode;
  }

  @Override public String toString() {
    return "TravelModePresenter{}";
  }
}
