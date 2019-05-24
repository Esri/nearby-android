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
package com.esri.android.nearbyplaces.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.esri.arcgisruntime.geometry.Point;

/**
 * Represents a place returned from the geocoding service.
 */
public final class Place implements Comparable<Place> {

  private final String mName;

  @Nullable
  private final String mType;
  @Nullable
  private final Point mLocation;
  @Nullable
  private final String mAddress;
  @Nullable
  private final String mURL;
  @Nullable
  private final String mPhone;
  @Nullable
  private  String mBearing;
  private  long mDistance;

  public Place(final String name, @Nullable final String type, @Nullable final Point location, @Nullable final String address, @Nullable final String URL, @Nullable final String phone,
      @Nullable final String bearing, final long distance) {
    mName = name;
    mType = type;
    mLocation = location;
    mAddress = address;
    mURL = URL;
    mPhone = phone;
    mBearing = bearing;
    mDistance = distance;
  }

  public String getName() {
    return mName;
  }

  @Nullable public String getType() {
    return mType;
  }

  @Nullable public Point getLocation() {
    return mLocation;
  }

  @Nullable public String getAddress() {
    return mAddress;
  }

  @Nullable public String getURL() {
    return mURL;
  }

  @Nullable public String getPhone() {
    return mPhone;
  }

  @Nullable public String getBearing() {
    return mBearing;
  }

  public long getDistance() { return  mDistance; }

  public void setBearing(final String bearing){
    mBearing = bearing;
  }

  public void setDistance(final long d){
    mDistance =d;
  }
  @Override public String toString() {
    return "Place{" +
        "mName='" + mName + '\'' +
        ", mType='" + mType + '\'' +
        ", mLocation=" + mLocation +
        ", mAddress='" + mAddress + '\'' +
        ", mURL='" + mURL + '\'' +
        ", mPhone='" + mPhone + '\'' +
        ", mBearing='" + mBearing + '\'' +
        ", mDistance='" + mDistance + '\'' +
        '}';
  }

  @Override public int compareTo(@NonNull Place anotherPlace) {
    int result = 0;
    if (mDistance > anotherPlace.mDistance){
      result = 1;
    }
    if (mDistance < anotherPlace.mDistance){
      result = -1;
    }
    return  result;
  }
}
