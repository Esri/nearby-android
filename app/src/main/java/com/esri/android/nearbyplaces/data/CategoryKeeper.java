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

import com.esri.android.nearbyplaces.R;
import com.esri.android.nearbyplaces.filter.FilterItem;

import java.util.ArrayList;
import java.util.List;

public class CategoryKeeper {
    private static CategoryKeeper instance = null;
    private final ArrayList<FilterItem> categories = new ArrayList<>();

    private CategoryKeeper(){
      categories.add(new FilterItem("Bar", R.drawable.ic_local_bar_grey_48dp, true, R.drawable.ic_local_bar_blue_48dp));
      categories.add(new FilterItem("Coffee Shop", R.drawable.ic_local_cafe_grey_48dp, true, R.drawable.ic_local_cafe_blue_48dp));
      categories.add(new FilterItem("Food", R.drawable.ic_local_dining_grey_48dp, true, R.drawable.ic_local_dining_blue_48dp));
      categories.add(new FilterItem("Hotel", R.drawable.ic_local_hotel_grey_48dp, true, R.drawable.ic_local_hotel_blue_48dp));
      categories.add(new FilterItem("Pizza", R.drawable.ic_local_pizza_gray_48dp, false, R.drawable.ic_local_pizza_blue_48dp));
    }

    public static CategoryKeeper getInstance(){
      if (instance == null){
        instance = new CategoryKeeper();
      }
      return instance;
    }

    public ArrayList<FilterItem> getCategories(){
      return categories;
    }

    public List<String> getSelectedTypes(){
        List<String> selectedTypes = new ArrayList<>();
        for (FilterItem item : categories){
            if (!item.getSelected()){
                // Because places with food are sub-categorized by
                // food type, add them to the filter list.
                if (item.getTitle().equalsIgnoreCase("Food")){
                    selectedTypes.addAll(CategoryHelper.foodTypes);
                }else {
                    selectedTypes.add(item.getTitle());
                }
            }
        }
        return selectedTypes;
    }
}
