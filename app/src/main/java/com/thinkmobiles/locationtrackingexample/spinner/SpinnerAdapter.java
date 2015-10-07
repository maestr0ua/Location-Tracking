package com.thinkmobiles.locationtrackingexample.spinner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.thinkmobiles.locationtrackingexample.Constants;
import com.thinkmobiles.locationtrackingexample.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by klim on 17.09.15.
 */
public final class SpinnerAdapter extends BaseAdapter {

    private List<String> mItems = new ArrayList<>();
    private LayoutInflater mInflater;

    public SpinnerAdapter(Context _context) {
        super();
        mInflater = LayoutInflater.from(_context);
    }

    public void addItems(List<String> _strings) {
        mItems.addAll(_strings);
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
        textView.setText(getTitle(position));
        ImageView imageView = (ImageView) view.findViewById(R.id.spinner_drop_down_item_icon);
        imageView.setImageResource(Constants.icons[position]);

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
        imageView.setImageResource(Constants.icons[position]);
        textView.setText(getTitle(position));
        return view;
    }

    private String getTitle(int position) {
        return position >= 0 && position < mItems.size() ? mItems.get(position) : "";
    }
}
