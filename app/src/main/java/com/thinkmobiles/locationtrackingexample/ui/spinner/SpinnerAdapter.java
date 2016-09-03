package com.thinkmobiles.locationtrackingexample.ui.spinner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.thinkmobiles.locationtrackingexample.R;
import com.thinkmobiles.locationtrackingexample.route.RouteMode;

import java.util.ArrayList;
import java.util.List;

public final class SpinnerAdapter extends BaseAdapter {

    private List<RouteMode> mItems = new ArrayList<>();
    private LayoutInflater mInflater;

    public SpinnerAdapter(Context context) {
        super();
        mInflater = LayoutInflater.from(context);
    }

    public void addItems(List<RouteMode> items) {
        mItems.clear();
        mItems.addAll(items);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {
        if (view == null || !view.getTag().toString().equals("DROPDOWN")) {
            view = mInflater.inflate(R.layout.toolbar_spinner_item_dropdown, parent, false);
            view.setTag("DROPDOWN");
        }

        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        ImageView imageView = (ImageView) view.findViewById(R.id.spinner_drop_down_item_icon);

        textView.setText(RouteMode.getTitle(position));
        imageView.setImageResource(RouteMode.getIcon(position));

        return view;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null || !view.getTag().toString().equals("NON_DROPDOWN")) {
            view = mInflater.inflate(R.layout.
                    toolbar_spinner_item_actionbar, parent, false);
            view.setTag("NON_DROPDOWN");
        }
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        ImageView imageView = (ImageView) view.findViewById(R.id.spinner_item_icon);

        imageView.setImageResource(RouteMode.getIcon(position));
        textView.setText(RouteMode.getTitle(position));
        return view;
    }

}
