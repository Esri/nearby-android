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

package com.esri.android.nearbyplaces.places;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.esri.android.nearbyplaces.R;
import com.esri.android.nearbyplaces.data.CategoryHelper;
import com.esri.android.nearbyplaces.data.Place;
import com.esri.arcgisruntime.geometry.Envelope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This fragment is responsible for presenting the list of places and supporting view actions.
 * It's the View in the MVP pattern.
 */
public class PlacesFragment extends Fragment implements PlacesContract.View {

  private PlacesContract.Presenter mPresenter;

  private PlacesFragment.PlacesAdapter mPlaceAdapter;

  private ProgressDialog mProgressDialog;

  public PlacesFragment() {

  }

  public static PlacesFragment newInstance() {
    return new PlacesFragment();

  }

  @Override
  public final void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // retain this fragment
    setRetainInstance(true);
    final List<Place> placeList = new ArrayList<>();

    mPlaceAdapter = new PlacesFragment.PlacesAdapter(getContext(), R.id.placesContainer, placeList);


  }

  @Nullable
  @Override
  public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
      final Bundle savedInstance) {
    View view = super.onCreateView(inflater, container, savedInstance);

    RecyclerView mPlacesView = (RecyclerView) inflater.inflate(
        R.layout.place_recycler_view, container, false);

    mPlacesView.setLayoutManager(new LinearLayoutManager(mPlacesView.getContext()));
    mPlacesView.setAdapter(mPlaceAdapter);

    return mPlacesView;
  }

  @Override
  public final void showNearbyPlaces(final List<Place> places) {
    if (places.isEmpty()) {
      showMessage(getString(R.string.no_places_found));
    } else {
      Collections.sort(places);
      mPlaceAdapter.setPlaces(places);
      mPlaceAdapter.notifyDataSetChanged();
    }
    mProgressDialog.dismiss();
  }

  @Override
  public void showProgressIndicator(final String message) {
    if (mProgressDialog == null) {
      mProgressDialog = new ProgressDialog(getActivity());
    }
    mProgressDialog.dismiss();
    mProgressDialog.setTitle(getString(R.string.nearby_places));
    mProgressDialog.setMessage(message);
    mProgressDialog.show();
  }

  @Override
  public void showMessage(final String message) {
    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
  }

  @Override
  public final void setPresenter(final PlacesContract.Presenter presenter) {
    mPresenter = presenter;
  }

  public class PlacesAdapter extends RecyclerView.Adapter<PlacesFragment.RecyclerViewHolder> {

    private List<Place> mPlaces = Collections.emptyList();

    public PlacesAdapter(final Context context, final int resource, final List<Place> places) {
      mPlaces = places;
    }

    public final void setPlaces(final List<Place> places) {
      mPlaces = places;
      notifyDataSetChanged();
    }

    @Override
    public final PlacesFragment.RecyclerViewHolder
    onCreateViewHolder(final ViewGroup parent, final int viewType) {
      final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      final View itemView = inflater.inflate(R.layout.place, parent, false);
      return new PlacesFragment.RecyclerViewHolder(itemView);
    }

    @Override
    public final void onBindViewHolder(final PlacesFragment.RecyclerViewHolder holder,
        final int position) {
      final Place place = mPlaces.get(position);
      holder.placeName.setText(place.getName());
      holder.address.setText(place.getAddress());
      final Drawable drawable = assignIcon(position);
      holder.icon.setImageDrawable(drawable);
      holder.bearing.setText(place.getBearing());
      holder.distance.setText(place.getDistance() + getString(R.string.m));
      holder.bind(place);
    }

    @Override
    public final int getItemCount() {
      return mPlaces.size();
    }

    private Drawable assignIcon(final int position) {
      final Place p = mPlaces.get(position);
      return CategoryHelper.getDrawableForPlace(p, getActivity());
    }
  }

  @Override
  public final void onStart() {
    super.onStart();
  }


  @Override
  public final void onStop() {
    super.onStop();
  }

  public class RecyclerViewHolder extends RecyclerView.ViewHolder {

    private final TextView placeName;
    private final TextView address;
    private final ImageView icon;
    private final TextView bearing;
    private final TextView distance;

    public RecyclerViewHolder(final View itemView) {
      super(itemView);
      placeName = itemView.findViewById(R.id.placeName);
      address = itemView.findViewById(R.id.placeAddress);
      icon = itemView.findViewById(R.id.placeTypeIcon);
      bearing = itemView.findViewById(R.id.placeBearing);
      distance = itemView.findViewById(R.id.placeDistance);
    }

    public final void bind(final Place place){
      itemView.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(final View v) {
          final Envelope envelope = mPresenter.getExtentForNearbyPlaces();
          final Intent intent = PlacesActivity.createMapIntent(getActivity(),envelope);
          intent.putExtra(getString(R.string.place_detail), place.getName());
          startActivity(intent);
        }
      });
    }
  }

}
