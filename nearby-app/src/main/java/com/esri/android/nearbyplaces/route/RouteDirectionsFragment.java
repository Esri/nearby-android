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
package com.esri.android.nearbyplaces.route;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.esri.android.nearbyplaces.R;
import com.esri.android.nearbyplaces.map.MapActivity;
import com.esri.arcgisruntime.tasks.networkanalysis.DirectionManeuver;
import com.esri.arcgisruntime.tasks.networkanalysis.DirectionManeuverType;

import java.util.ArrayList;
import java.util.List;

public class RouteDirectionsFragment extends Fragment {

  private List<DirectionManeuver> mDirectionManeuvers = new ArrayList<>();
  private final static String TAG = RouteDirectionsFragment.class.getSimpleName();
  private DirectionsListAdapter mAdapter = null;

  public static RouteDirectionsFragment newInstance(){
    return new RouteDirectionsFragment();
  }

  @Override
  public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
      final Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.route_direction_list, container,false);

    // Set up the header
    ImageView backBtn = getActivity().findViewById(R.id.btnCloseDirections);
    backBtn.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        ((MapActivity)getActivity()).restoreRouteView();
      }
    });

    // Hide the action bar
    final ActionBar ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
    if (ab != null){
      ab.hide();
    }

    // Setup list adapter
    final ListView listView = view.findViewById(R.id.directions_list);
    mAdapter = new DirectionsListAdapter(mDirectionManeuvers);
    listView.setAdapter(mAdapter);

    // When directions are tapped, show selected route section
    // highlighted on map and zoomed in with route section description
    // overlaid on map
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ((MapActivity)getActivity()).showRouteDetail(position);
      }
    });


    return view;
  }

  public final void setRoutingDirections(final List<DirectionManeuver> directions){

    mDirectionManeuvers = directions;
    if (mAdapter != null){
      mAdapter.notifyDataSetChanged();
    }

  }
  /**
   * List adapter for the list of route directions.
   */
  private class DirectionsListAdapter extends ArrayAdapter<DirectionManeuver> {
    public DirectionsListAdapter(final List<DirectionManeuver> directions) {
      super(getActivity(), 0, directions);
    }

    @NonNull @Override
    public final View getView(final int position, final View convertView, @NonNull final ViewGroup parent) {
      // Inflate view if we haven't been given one to reuse
      View v = convertView;
      if (convertView == null) {
        v = getActivity().getLayoutInflater().inflate(R.layout.route_direction_list_item, parent, false);
      }

      // Configure the view for this item
      final DirectionManeuver direction = getItem(position);
      final ImageView imageView = v.findViewById(R.id.directions_maneuver_imageview);
      final Drawable drawable = getRoutingIcon(direction != null ? direction.getManeuverType() : null);
      if (drawable != null) {
        imageView.setImageDrawable(drawable);
      }
      TextView textView = v.findViewById(R.id.directions_text_textview);
      textView.setText(direction.getDirectionText());
      textView = v.findViewById(R.id.directions_length_textview);
      final String lengthString = String.format("%.1f meters", direction.getLength());
      textView.setText(lengthString);
      return v;
    }

    private Drawable getRoutingIcon(final DirectionManeuverType maneuver) {

      try {
        Integer id = getResourceIdForManeuverType(maneuver);
        return ResourcesCompat.getDrawable(getActivity().getResources(),id,null);
      } catch (final Resources.NotFoundException e) {
        Log.w(RouteDirectionsFragment.TAG, "No drawable found for" + maneuver.name());
        return null;
      }
    }
  }

  /**
   * Return appropriate resource id for given type of direction
   */
  public Integer getResourceIdForManeuverType(DirectionManeuverType maneuverType){
    final int id;
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
}
