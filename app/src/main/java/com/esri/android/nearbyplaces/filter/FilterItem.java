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
package com.esri.android.nearbyplaces.filter;

/**
 * Created by sand8529 on 7/28/16.
 */
public class FilterItem {

  private String mTitle;
  private int mIconId;
  private boolean mSelected;

  public FilterItem (String title, int icon){
    mTitle = title;
    mIconId = icon;
    mSelected = false;
  }
  public FilterItem (String title, int icon, boolean s){
    mTitle = title;
    mIconId = icon;
    mSelected = s;
  }
  public FilterItem(){}

  public int getIconId() {
    return mIconId;
  }

  public void setIconId(int iconId) {
    mIconId = iconId;
  }

  public String getTitle() {
    return mTitle;
  }

  public void setTitle(String mTitle) {
    this.mTitle = mTitle;
  }

  public boolean getSelected() { return mSelected ;}

  public void setSelected(boolean selected){ mSelected = selected; }
}
