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

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.esri.android.nearbyplaces.R;
import com.esri.android.nearbyplaces.data.CategoryHelper;
import com.esri.android.nearbyplaces.data.LocationService;
import com.esri.android.nearbyplaces.data.Place;
import com.esri.arcgisruntime.geometry.Envelope;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PlacesFragment extends Fragment implements PlacesContract.View,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

  private PlacesContract.Presenter mPresenter;

  private PlacesFragment.PlacesAdapter mPlaceAdapter;

  private static final String TAG = PlacesFragment.class.getSimpleName();

  private GoogleApiClient mGoogleApiClient;

  private FragmentListener mCallback;

  private ProgressDialog mProgressDialog;

  public PlacesFragment(){

  }
  public static  PlacesFragment newInstance(){
    return new PlacesFragment();

  }
  @Override
  public final void onCreate(final Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    // retain this fragment
    setRetainInstance(true);

    mPlaceAdapter = new PlacesFragment.PlacesAdapter(new ArrayList<>());

    mCallback.onCreationComplete();
  }

  @Nullable
  @Override
  public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
      final Bundle savedInstance){

    RecyclerView mPlacesView = (RecyclerView) inflater.inflate(
        R.layout.place_recycler_view, container, false);

    mPlacesView.setLayoutManager(new LinearLayoutManager(mPlacesView.getContext()));
    mPlacesView.setAdapter(mPlaceAdapter);

    return mPlacesView;
  }

  @Override
  public void onAttach(final Context activity) {
    super.onAttach(activity);
    // This makes sure that the container activity has implemented
    // the callback interface. If not, it throws an exception
    try {
      mCallback = (FragmentListener) activity;
    } catch (final ClassCastException e) {
      throw new ClassCastException(activity.toString()
          + " must implement FragmentListener");
    }
  }

  @Override public final void showNearbyPlaces(final List<Place> places) {
    if (places.isEmpty()){
      showMessage("No places found");
    }else{
      Collections.sort(places);
      mPlaceAdapter.setPlaces(places);
      mPlaceAdapter.notifyDataSetChanged();
    }
    mProgressDialog.dismiss();
  }

  @Override public void showProgressIndicator(final String message) {
    if (mProgressDialog == null){
      mProgressDialog = new ProgressDialog(getActivity());
    }
    mProgressDialog.dismiss();
    mProgressDialog.setTitle(getString(R.string.nearby_places));
    mProgressDialog.setMessage(message);
    mProgressDialog.show();

  }


  @Override public void showMessage(final String message) {
    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
  }


  @Override public final void setPresenter(final PlacesContract.Presenter presenter) {
    mPresenter = presenter;
    // Create an instance of GoogleAPIClient.
    if (mGoogleApiClient == null) {
      mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
          .addConnectionCallbacks(this)
          .addOnConnectionFailedListener(this)
          .addApi(LocationServices.API)
          .build();
    }
  }


  public  class PlacesAdapter extends RecyclerView.Adapter<PlacesFragment.RecyclerViewHolder> {

    private List<Place> mPlaces;

    public PlacesAdapter(final List<Place> places) {
      mPlaces = places;
    }

    public final void setPlaces(final List<Place> places) {
      mPlaces = places;
      notifyDataSetChanged();
    }

    @Override public final PlacesFragment.RecyclerViewHolder onCreateViewHolder(final ViewGroup parent,
        final int viewType) {
      final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      final View itemView = inflater.inflate(R.layout.place, parent, false);
      return new PlacesFragment.RecyclerViewHolder(itemView);
    }


    @Override public final void onBindViewHolder(final PlacesFragment.RecyclerViewHolder holder, final int position) {
      final Place place = mPlaces.get(position);
      holder.placeName.setText(place.getName());
      holder.address.setText(place.getAddress());
      final Drawable drawable = assignIcon(position);
      holder.icon.setImageDrawable(drawable);
      holder.bearing.setText(place.getBearing());
      holder.distance.setText(place.getDistance() + getString(R.string.m));
      holder.bind(place);
    }

    @Override public final int getItemCount() {
      return mPlaces.size();
    }

    private Drawable assignIcon(final int position){
      final Place p = mPlaces.get(position);
      return CategoryHelper.getDrawableForPlace(p, getActivity());
    }
  }


  @Override
  public final void onConnected(@Nullable final Bundle bundle) {
    if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
      final Task getLocationTask = LocationServices.getFusedLocationProviderClient(this.getContext()).getLastLocation();
      getLocationTask.addOnCompleteListener(this.getActivity(), new OnCompleteListener<Location>() {
        @Override
        public void onComplete(@NonNull Task<Location> task) {
          startPresenter((Location) getLocationTask.getResult());
        }
      });
    } else {
      startPresenter(null);
    }
  }

  @Override public void onConnectionSuspended(final int i) {
    Log.i(TAG, getString(R.string.location_connection_lost));
    mGoogleApiClient.connect();
  }
  @Override public final void onStart() {
    mGoogleApiClient.connect();
    super.onStart();
  }

  @Override  public final void onStop() {
    mGoogleApiClient.disconnect();
    super.onStop();
  }

  @Override public void onConnectionFailed(@NonNull final ConnectionResult connectionResult) {
    Toast.makeText(getContext(), getString(R.string.google_location_connection_problem),Toast.LENGTH_LONG).show();
    Log.e(TAG, getString(R.string.google_location_problem) + connectionResult.getErrorMessage());
  }


  /**
   * Starts the presenter with the given last location. If the location is null, default to a
   * location in downtown Portland.
   */
  private void startPresenter(Location location) {
    if (location == null) {
      // Default to downtown Portland
      location = new Location("Default");
      location.setLatitude(45.5155);
      location.setLongitude( -122.676483);
    }
    Log.i(PlacesFragment.TAG, getString(R.string.latlong) + location.getLatitude() + "/" + location.getLongitude());
    mPresenter.setLocation(location);
    LocationService.getInstance().setCurrentLocation(location);
    mPresenter.start();
  }

  /**
   * Signals to the activity that this fragment has
   * completed creation activities.
   */
  public interface FragmentListener{
    void onCreationComplete();
  }
  public class RecyclerViewHolder extends RecyclerView.ViewHolder {

    public final TextView placeName;
    public final TextView address;
    public final ImageView icon;
    public final TextView bearing;
    public final TextView distance;

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
