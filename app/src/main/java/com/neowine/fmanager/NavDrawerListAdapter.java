package com.neowine.fmanager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class NavDrawerListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<NavDrawerItem> navDrawerItems;

    public NavDrawerListAdapter(Context context, ArrayList<NavDrawerItem> navDrawerItems){
        this.context = context;
        this.navDrawerItems = navDrawerItems;
    }

    @Override
    public int getCount() {
        return navDrawerItems.size();
    }

    @Override
    public Object getItem(int position) {
        return navDrawerItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
        }

        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.navIcon);
        ImageView imgIcon2 = (ImageView) convertView.findViewById(R.id.navIcon2);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.navTitle);
        LinearLayout navIcon_layout = (LinearLayout) convertView.findViewById(R.id.navIcon_layout);
        LinearLayout navMain_layout = (LinearLayout) convertView.findViewById(R.id.navMain_layout);

        imgIcon.setImageResource(navDrawerItems.get(position).getIcon());
        imgIcon2.setImageResource(navDrawerItems.get(position).getIcon());
        txtTitle.setText(navDrawerItems.get(position).getTitle());

        if (navDrawerItems.get(position).getTitle().equals("Local") || navDrawerItems.get(position).getTitle().equals("Library")) {
            navIcon_layout.setVisibility(View.GONE);
            imgIcon2.setVisibility(View.VISIBLE);
            navMain_layout.setBackgroundColor(Color.rgb(255, 255, 255));
        } else {
            navIcon_layout.setVisibility(View.VISIBLE);
            imgIcon2.setVisibility(View.GONE);
            navMain_layout.setBackgroundColor(Color.rgb(237, 237, 237));
        }

        return convertView;
    }

}