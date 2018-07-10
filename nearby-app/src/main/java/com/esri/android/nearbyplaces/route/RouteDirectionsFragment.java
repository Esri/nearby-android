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
import com.esri.android.nearbyplaces.data.CategoryHelper;
import com.esri.android.nearbyplaces.map.MapActivity;
import com.esri.arcgisruntime.tasks.networkanalysis.DirectionManeuver;
import com.esri.arcgisruntime.tasks.networkanalysis.DirectionManeuverType;

import java.util.ArrayList;
import java.util.List;

/**
 * This fragment is displayed over the map and shows a list of turn by turn directions
 * along the displayed route.
 */
public class RouteDirectionsFragment extends Fragment {

  private List<DirectionManeuver> mDirectionManeuvers = new ArrayList<>();
  private final static String TAG = RouteDirectionsFragment.class.getSimpleName();
  private DirectionsListAdapter mAdapter;

  public static RouteDirectionsFragment newInstance(){
    return new RouteDirectionsFragment();
  }

  @Override
  public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
      final Bundle savedInstanceState) {
    View view1 = super.onCreateView(inflater, container, savedInstanceState);
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
    // overlayed on map
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
      final String lengthString = String.format(getString(R.string.meter_format), direction.getLength());
      textView.setText(lengthString);
      return v;
    }

    private Drawable getRoutingIcon(final DirectionManeuverType maneuver) {
      try {
        Integer id = CategoryHelper.getResourceIdForManeuverType(maneuver);
        return ResourcesCompat.getDrawable(getActivity().getResources(),id,null);
      } catch (final Resources.NotFoundException e) {
        Log.w(RouteDirectionsFragment.TAG, getString(R.string.no_drawable_found) + maneuver.name());
        return null;
      }
    }

  }
}
