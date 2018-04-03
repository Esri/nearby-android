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

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;

import com.esri.android.nearbyplaces.R;
import com.esri.android.nearbyplaces.filter.FilterItem;
import com.esri.arcgisruntime.tasks.networkanalysis.DirectionManeuverType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A helper class for managing category icons and logic
 */
public class CategoryHelper {
  static final List<FilterItem> categories = Arrays.asList(
          new FilterItem("Bar", R.drawable.ic_local_bar_grey_48dp, true, R.drawable.ic_local_bar_blue_48dp),
          new FilterItem("Coffee Shop", R.drawable.ic_local_cafe_grey_48dp, true, R.drawable.ic_local_cafe_blue_48dp),
          new FilterItem("Food", R.drawable.ic_local_dining_grey_48dp, true, R.drawable.ic_local_dining_blue_48dp),
          new FilterItem("Hotel", R.drawable.ic_local_hotel_grey_48dp, true, R.drawable.ic_local_hotel_blue_48dp),
          new FilterItem("Pizza", R.drawable.ic_local_pizza_gray_48dp, false, R.drawable.ic_local_pizza_blue_48dp),
          new FilterItem("Museum", R.drawable.ic_museum_grey, false, R.drawable.ic_museum_blue),
          new FilterItem("Trail", R.drawable.ic_hiking_grey, false, R.drawable.ic_hike_blue),
          new FilterItem("Winery", R.drawable.ic_winery_grey, false, R.drawable.ic_wine_blue),
          new FilterItem("Waterfall", R.drawable.ic_waterfall_grey, false, R.drawable.ic_waterfall_blue)
  );

  static final List<String> foodTypes = Arrays.asList(
        "African Food",
        "American Food",
        "Argentinean Food",
        "Australian Food",
        "Austrian Food",
        "Bakery",
        "BBQ and Southern Food",
        "Belgian Food",
        "Bistro",
        "Brazilian Food",
        "Breakfast",
        "Brewpub",
        "British Isles Food",
        "Burgers",
        "Cajun and Creole Food",
        "Californian Food",
        "Caribbean Food",
        "Chicken Restaurant",
        "Chilean Food",
        "Chinese Food",
        "Continental Food",
        "Creperie",
        "East European Food",
        "Fast Food",
        "Filipino Food",
        "Fondue",
        "French Food",
        "Fusion Food",
        "German Food",
        "Greek Food",
        "Grill",
        "Hawaiian Food",
        "Ice Cream Shop",
        "Indian Food",
        "Indonesian Food",
        "International Food",
        "Irish Food",
        "Italian Food",
        "Japanese Food",
        "Korean Food",
        "Kosher Food",
        "Latin American Food",
        "Malaysian Food",
        "Mexican Food",
        "Middle Eastern Food",
        "Moroccan Food",
        "Other Restaurant",
        "Pastries",
        "Polish Food",
        "Portuguese Food",
        "Russian Food",
        "Sandwich Shop",
        "Scandinavian Food",
        "Seafood",
        "Snacks",
        "South American Food",
        "Southeast Asian Food",
        "Southwestern Food",
        "Spanish Food",
        "Steak House",
        "Sushi",
        "Swiss Food",
        "Tapas",
        "Thai Food",
        "Turkish Food",
        "Vegetarian Food",
        "Vietnamese Food");

  /**
   * Checks a specific type of food (e.g. Thai Food) against
   * a known list of food types.
   * @param foodType - String representing a type of food
   * @return - Returns a String, either 'Food' or if not found in list, returns the input.
   */
  private static String getCategoryForFoodType(final String foodType){
    String category = foodType;
    if (foodTypes.contains(foodType)){
      category = "Food";
    }
    return category;
  }

  /**
   * Assign appropriate drawable given place type
   * @param p - Place
   * @return - Drawable
   */
  public static Integer getResourceIdForPlacePin(final Place p){
    final String category = CategoryHelper.getCategoryForFoodType(p.getType());
    final Integer d;
    switch (category){
      case "Pizza":
        d = R.drawable.pizza_pin;
        break;
      case "Hotel":
        d = R.drawable.hotel_pin;
        break;
      case "Food":
        d = R.drawable.restaurant_pin;
        break;
      case "Bar or Pub":
        d = R.drawable.bar_pin;
        break;
      case "Coffee Shop":
        d = R.drawable.cafe_pin;
        break;
      case "Museum":
        d = R.drawable.museumred;
        break;
      case "Trail":
        d = R.drawable.hikingred;
        break;
      case "Winery":
        d = R.drawable.wineryred;
        break;
      case "Waterfall":
        d = R.drawable.waterfallred;
        break;
      default:
        d = R.drawable.empty_pin;
    }
    return d;
  }

  /**
   * Return appropriate resource id for given type of direction
   */
  public static Integer getResourceIdForManeuverType(DirectionManeuverType maneuverType){
    final Integer id;
    switch (maneuverType) {
      case STRAIGHT :
        id = R.drawable.ic_routing_straight_arrow;
        break;
      case BEAR_LEFT :
        id = R.drawable.ic_routing_bear_left;
        break;
      case BEAR_RIGHT :
        id = R.drawable.ic_routing_bear_right;
        break;
      case TURN_LEFT :
        id = R.drawable.ic_routing_turn_left;
        break;
      case TURN_RIGHT :
        id = R.drawable.ic_routing_turn_right;
        break;
      case SHARP_LEFT :
        id = R.drawable.ic_routing_turn_sharp_left;
        break;
      case SHARP_RIGHT :
        id = R.drawable.ic_routing_turn_sharp_right;
        break;
      case U_TURN :
        id = R.drawable.ic_routing_u_turn;
        break;
      case FERRY :
        id = R.drawable.ic_routing_take_ferry;
        break;
      case ROUNDABOUT :
        id = R.drawable.ic_routing_get_on_roundabout;
        break;
      case HIGHWAY_MERGE :
        id = R.drawable.ic_routing_merge_onto_highway;
        break;
      case HIGHWAY_CHANGE :
        id = R.drawable.ic_routing_highway_change;
        break;
      case FORK_CENTER :
        id = R.drawable.ic_routing_take_center_fork;
        break;
      case FORK_LEFT :
        id = R.drawable.ic_routing_take_fork_left;
        break;
      case FORK_RIGHT :
        id = R.drawable.ic_routing_take_fork_right;
        break;
      case END_OF_FERRY :
        id = R.drawable.ic_routing_get_off_ferry;
        break;
      case RAMP_RIGHT :
        id = R.drawable.ic_routing_take_ramp_right;
        break;
      case RAMP_LEFT :
        id = R.drawable.ic_routing_take_ramp_left;
        break;
      case TURN_LEFT_RIGHT :
        id = R.drawable.ic_routing_left_right;
        break;
      case TURN_RIGHT_LEFT :
        id = R.drawable.ic_routing_right_left;
        break;
      case TURN_RIGHT_RIGHT :
        id = R.drawable.ic_routing_right_right;
        break;
      case TURN_LEFT_LEFT :
        id = R.drawable.ic_routing_left_left;
        break;
      case STOP :
        id = R.drawable.end_route_pin;
        break;
      case DEPART:
        id = R.drawable.route_pin_start;
        break;
      case HIGHWAY_EXIT :
      case TRIP_ITEM :
      case PEDESTRIAN_RAMP :
      case ELEVATOR :
      case ESCALATOR :
      case STAIRS :
      case DOOR_PASSAGE :
      default :
        Log.w("CategoryHelper", maneuverType.name() + "not supported");
        return null;
    }
    return  id;
  }

  /**
   * Return appropriate id for given type of place.
   * @param p - Place
   * @return - Integer representing icon id
   */
  public static Integer getPinForCenterPlace(final Place p){
    final String category = CategoryHelper.getCategoryForFoodType(p.getType());
    final Integer d;
    switch (category){
      case "Pizza":
        d = R.drawable.blue_pizza_pin;
        break;
      case "Hotel":
        d = R.drawable.blue_hotel_pin;
        break;
      case "Food":
        d = R.drawable.blue_rest_pin;
        break;
      case "Bar or Pub":
        d = R.drawable.blue_bar_pin;
        break;
      case "Coffee Shop":
        d = R.drawable.blue_cafe_pin;
        break;
      case "Museum":
        d = R.drawable.museumblue;
        break;
      case "Trail":
        d = R.drawable.hikingblue;
        break;
      case "Winery":
        d = R.drawable.wineblue;
        break;
      case "Waterfall":
        d = R.drawable.waterfallblue;
        break;
      default:
        d = R.drawable.blue_empty_pin;
    }
    return d;
  }

  /**
   * Return appropriate drawable base on place type
   * @param p - Place item
   * @param a - Activity
   * @return - Drawable
   */
  public static Drawable getDrawableForPlace(final Place p, final Activity a){
    final String placeType = p.getType();
    final String category = CategoryHelper.getCategoryForFoodType(placeType);
    final Drawable d;
    switch (category){
      case "Pizza":
        d = ResourcesCompat.getDrawable(a.getResources(), R.drawable.ic_local_pizza_black_24dp,null);
        break;
      case "Hotel":
        d = ResourcesCompat.getDrawable(a.getResources(), R.drawable.ic_hotel_black_24dp,null);
        break;
      case "Food":
        d = ResourcesCompat.getDrawable(a.getResources(), R.drawable.ic_local_dining_black_24dp,null);
        break;
      case "Bar or Pub":
        d = ResourcesCompat.getDrawable(a.getResources(), R.drawable.ic_local_bar_black_24dp,null);
        break;
      case "Coffee Shop":
        d = ResourcesCompat.getDrawable(a.getResources(), R.drawable.ic_local_cafe_black_24dp,null);
        break;
      case "Waterfall":
        d = ResourcesCompat.getDrawable(a.getResources(), R.drawable.ic_waterfall_black,null);
        break;
      case "Winery":
        d = ResourcesCompat.getDrawable(a.getResources(), R.drawable.ic_wine_black,null);
        break;
      case "Trail":
        d = ResourcesCompat.getDrawable(a.getResources(), R.drawable.ic_hiking_black,null);
        break;
      case "Museum":
        d = ResourcesCompat.getDrawable(a.getResources(), R.drawable.ic_museum_black,null);
        break;
      default:
        d = ResourcesCompat.getDrawable(a.getResources(), R.drawable.ic_place_black_24dp,null);
    }
    return d;
  }

  /**
   * Return a List of FilterItem objects.
   * @return - List<FilterItem></FilterItem>
   */
  public static List<FilterItem> getCategories(){
    return categories;
  }

  /**
   * Return a list of strings that have been selected in the filter.
   * @return - List<String></String>
   */
  public static List<String> getSelectedTypes(){
    final List<String> selectedTypes = new ArrayList<>();
    for (final FilterItem item : categories){
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
