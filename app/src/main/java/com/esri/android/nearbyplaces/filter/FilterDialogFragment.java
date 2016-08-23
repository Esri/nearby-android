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

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.*;
import com.esri.android.nearbyplaces.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sand8529 on 7/28/16.
 */
public class FilterDialogFragment extends DialogFragment implements FilterContract.View {
  private FilterContract.Presenter mPresenter;
  private FilterItemAdapter mFilterItemAdapter;
  private FilterContract.FilterView mFilterView;

  public FilterDialogFragment(){}

  public static FilterDialogFragment newInstance(){
    return new FilterDialogFragment();
  }
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    getDialog().setTitle(R.string.filter_dialog);
    View view = inflater.inflate(R.layout.place_filter, container,false);
    ListView listView = (ListView) view.findViewById(R.id.filterView);
    List<FilterItem> filters = mPresenter.getFilteredCategories();
    ArrayList<FilterItem> arrayList = new ArrayList<>();
    arrayList.addAll(filters);
    mFilterItemAdapter = new FilterItemAdapter(getActivity(), arrayList);
    listView.setAdapter(mFilterItemAdapter);

    // Listen for Cancel/Apply
    Button cancel = (Button) view.findViewById(R.id.btnCancel);
    cancel.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        dismiss();
      }
    });
    Button apply = (Button) view.findViewById(R.id.btnApply);
    apply.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Activity activity = getActivity();
        if (activity instanceof FilterContract.FilterView){
          ((FilterContract.FilterView) activity).onFilterDialogClose(true);
        }
        dismiss();
      }
    });
    return view;
  }

  @Override
  public void onCreate(Bundle savedBundleState){
    super.onCreate(savedBundleState);
    setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    mPresenter.start();
  }

  @Override public void setPresenter(FilterContract.Presenter presenter) {
    mPresenter = presenter;
  }



  public class FilterItemAdapter extends ArrayAdapter<FilterItem>{
    public FilterItemAdapter(Context context, ArrayList<FilterItem> items){
      super(context,0, items);
    }

    private class ViewHolder {
      Button btn;
      TextView txtName;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      ViewHolder holder = null;

      // Get the data item for this position
      final FilterItem item = getItem(position);
      // Check if an existing view is being reused, otherwise inflate the view
      if (convertView == null) {
        convertView = LayoutInflater.from(getActivity()).inflate(R.layout.filter_view, parent, false);
        holder = new ViewHolder();
        holder.btn = (Button) convertView.findViewById(R.id.categoryBtn);
        holder.txtName =  (TextView) convertView.findViewById(R.id.categoryName);
        convertView.setTag(holder);
      }else{
        holder = (ViewHolder) convertView.getTag();
      }
      // Lookup view for data population
      holder.txtName.setText(item.getTitle());

      if (!item.getSelected()){
        holder.btn.setBackgroundResource(item.getIconId());
        holder.btn.setAlpha(0.5f);
      }else{
        holder.btn.setBackgroundResource(item.getSelectedIconId());
        holder.btn.setAlpha(1.0f);
      }

      final int myPosition = position;
      final ViewHolder clickHolder = holder;
      // Attach listener that toggles selected state of category
      convertView.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          Drawable bm1 =  ResourcesCompat.getDrawable(getResources(),item.getIconId(),null);
          Drawable bm2 =  ResourcesCompat.getDrawable(getResources(),item.getSelectedIconId(),null);
          FilterItem clickedItem = getItem(myPosition);
          if (clickedItem.getSelected()){
            clickedItem.setSelected(false);
            clickHolder.btn.setBackgroundResource(item.getIconId());
            clickHolder.btn.setAlpha(0.5f);

          }else{
            clickedItem.setSelected(true);
            clickHolder.btn.setBackgroundResource(item.getSelectedIconId());
            clickHolder.btn.setAlpha(1.0f);
          }
        }
      });

      // Return the completed view to render on screen
      return convertView;
    }
  }

}
