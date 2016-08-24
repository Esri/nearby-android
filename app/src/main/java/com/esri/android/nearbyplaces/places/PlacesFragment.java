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

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.esri.android.nearbyplaces.PlaceListener;
import com.esri.android.nearbyplaces.R;
import com.esri.android.nearbyplaces.data.CategoryHelper;
import com.esri.android.nearbyplaces.data.LocationService;
import com.esri.android.nearbyplaces.data.Place;
import com.esri.android.nearbyplaces.filter.FilterContract;
import com.esri.android.nearbyplaces.map.MapActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class PlacesFragment extends Fragment implements PlacesContract.View,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

  private PlacesContract.Presenter mPresenter;

  private PlacesAdapter mPlaceAdapter;

  private RecyclerView mPlacesView;

  private static final String TAG = PlacesFragment.class.getSimpleName();

  private GoogleApiClient mGoogleApiClient;
  private Location mLastLocation;

  public PlacesFragment(){

  }
  public static  PlacesFragment newInstance(){
    return new PlacesFragment();

  }
  @Override
  public void onCreate(@NonNull Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    // retain this fragment
    setRetainInstance(true);
    List<Place> placeList = new ArrayList<>();

    mPlaceAdapter = new PlacesAdapter(getContext(), R.id.placesContainer,placeList);

    // Create an instance of GoogleAPIClient.
    if (mGoogleApiClient == null) {
      mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
          .addConnectionCallbacks(this)
          .addOnConnectionFailedListener(this)
          .addApi(LocationServices.API)
          .build();
    }
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstance){

    mPlacesView= (RecyclerView) inflater.inflate(
        R.layout.places_fragment2, container, false);

    mPlacesView.setLayoutManager(new LinearLayoutManager(mPlacesView.getContext()));
    mPlacesView.setAdapter(mPlaceAdapter);

    return mPlacesView;
  }

  @Override
  public void onResume() {
    super.onResume();
    mPresenter.start();

  }
  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
  }

  @Override public void showNearbyPlaces(List<Place> places) {
    mPlaceAdapter.setPlaces(places);
    mPlaceAdapter.notifyDataSetChanged();
  }

  @Override public void showProgressIndicator(final boolean active) {
    if (getView() == null){
      return;
    }
  }

  @Override public boolean isActive() {
    return false;
  }

  @Override public void setPresenter(PlacesContract.Presenter presenter) {
    mPresenter = checkNotNull(presenter);
  }


  public  class PlacesAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {

    private List<Place> mPlaces = Collections.emptyList();
    public PlacesAdapter(Context context, int resource, List<Place> places){
          mPlaces = places;
    }

    public void setPlaces(List<Place> places){
      checkNotNull(places);
      mPlaces = places;
      notifyDataSetChanged();
    }

    @Override public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      final View itemView = inflater.inflate(R.layout.place, parent, false);
      return new RecyclerViewHolder(itemView);
    }


    @Override public void onBindViewHolder(RecyclerViewHolder holder, int position) {
      Place place = mPlaces.get(position);
      holder.placeName.setText(place.getName());
      holder.address.setText(place.getAddress());
      Drawable drawable = assignIcon(position);
      holder.icon.setImageDrawable(drawable);
      holder.bind(place);
    }

    @Override public int getItemCount() {
      return mPlaces.size();
    }

    private Drawable assignIcon(int position){
      Place p = mPlaces.get(position);
      return CategoryHelper.getDrawableForPlace(p, getActivity());
    }
  }


  @Override public void onConnected(@Nullable Bundle bundle) {
    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
        mGoogleApiClient);
    if (mLastLocation != null){
      Log.i(TAG, "Latitude/longitude from FusedLocationApi " + mLastLocation.getLatitude() + "/" + mLastLocation.getLongitude());
      mPresenter.setLocation(mLastLocation);
      LocationService locationService = LocationService.getInstance();
      locationService.setCurrentLocation(mLastLocation);
      mPresenter.start();
    }
  }

  @Override public void onConnectionSuspended(int i) {

  }
  @Override public void onStart() {
    mGoogleApiClient.connect();
    super.onStart();
  }

  @Override  public void onStop() {
    mGoogleApiClient.disconnect();
    super.onStop();
  }

  @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

  }
  public class RecyclerViewHolder extends RecyclerView.ViewHolder {

    public TextView placeName;
    public TextView address;
    public ImageView icon;

    public RecyclerViewHolder(View itemView) {
      super(itemView);
      placeName = (TextView) itemView.findViewById(R.id.placeName);
      address = (TextView) itemView.findViewById(R.id.placeAddress);
      icon = (ImageView) itemView.findViewById(R.id.placeTypeIcon);
    }
    public void bind(final Place place){
      itemView.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          Intent intent = new Intent(getContext(),MapActivity.class);
          intent.putExtra("PLACE_DETAIL", place.getName());
          startActivity(intent);
        }
      });
    }

  }
}
