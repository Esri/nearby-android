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

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.DialogFragment;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.esri.android.nearbyplaces.R;
import com.esri.android.nearbyplaces.data.TravelMode;

/**
 * This fragment is responsible for presenting the list of travel modes and supporting view actions.
 * It's the View in the MVP pattern.
 */
public class TravelModeFragment extends DialogFragment implements TravelModeContract.View {
  private TravelModeContract.Presenter mPresenter;

  private TravelMode.TravelModeTypes mMode;
  private AppCompatTextView walkTxt, carTxt;

  @Override public void setPresenter(TravelModeContract.Presenter presenter) {
    mPresenter = presenter;
  }

  @Override
  public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
      final Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    getDialog().setTitle(R.string.choose_mode);
    final View view = inflater.inflate(R.layout.travel_model_layout, container,false);

    walkTxt = view.findViewById(R.id.txtWalk);
    carTxt = view.findViewById(R.id.txtCar);

    if (mPresenter.getTravelMode().equalsIgnoreCase(TravelMode.TravelModeTypes.Walk.name())) {
      showSelectedState(walkTxt, carTxt, true);
    } else {
      showSelectedState(walkTxt, carTxt, false);
    }

    View.OnClickListener walkListener = new View.OnClickListener() {
      @Override public void onClick(View view) {
        showSelectedState(walkTxt, carTxt, true);
        setTravelMode(TravelMode.TravelModeTypes.Walk);
        applyMode();
        dismiss();
      }
    };

    View.OnClickListener carListener = new View.OnClickListener() {
      @Override public void onClick(View view) {
        setTravelMode(TravelMode.TravelModeTypes.Drive);
        showSelectedState(walkTxt, carTxt, false);
        applyMode();
        dismiss();
      }
    };
    walkTxt.setOnClickListener(walkListener);
    carTxt.setOnClickListener(carListener);

    return view;
  }

  /**
   * Reflect selection state in text and drawable
   * @param txtView1 - TextView
   * @param txtView2 - TextView
   * @param selected - boolean
   */
  private void showSelectedState(AppCompatTextView txtView1, AppCompatTextView txtView2, boolean selected) {

    Drawable drawable1 = txtView1.getCompoundDrawablesRelative()[0];
    Drawable drawable2 = txtView2.getCompoundDrawablesRelative()[0];
    if (selected) {
      drawable1.setTint(getContext().getColor(R.color.colorPrimary));
      drawable2.setTint(getContext().getColor(R.color.colorBlack));
      txtView1.setTypeface(null, Typeface.BOLD);
      txtView2.setTypeface(Typeface.DEFAULT);
    } else {
      drawable1.setTint(getContext().getColor(R.color.colorBlack));
      drawable2.setTint(getContext().getColor(R.color.colorPrimary));
      txtView1.setTypeface(Typeface.DEFAULT);
      txtView2.setTypeface(null, Typeface.BOLD);
    }
  }
  @Override
  public final void onCreate(final Bundle savedBundleState){
    super.onCreate(savedBundleState);
    setStyle(android.app.DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    mPresenter.start();
  }

  /**
   * Set the travel mode
   * @param mode - TravelMode
   */
  private void setTravelMode(TravelMode.TravelModeTypes mode) {
    mMode = mode;
  }

  /**
   * Apply the travel mode
   */
  private void applyMode() {
    final Activity activity = getActivity();
    if (activity instanceof TravelModeContract.TravelModeView) {
      ((TravelModeContract.TravelModeView) getActivity()).onTravelModeClose(mMode);
    }
  }

  @Override public String toString() {
    return "TravelModeFragment{}";
  }
}
