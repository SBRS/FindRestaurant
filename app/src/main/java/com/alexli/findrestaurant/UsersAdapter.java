package com.alexli.findrestaurant;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by alexandrli on 22/09/17.
 */

public class UsersAdapter extends ArrayAdapter<ItemView>
{

    public UsersAdapter(Context context, ArrayList<ItemView> itemViews) {
        super(context, 0, itemViews);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ItemView itemView = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.view_item, parent, false);
        }
        // Lookup view for data population
        TextView txtName = (TextView) convertView.findViewById(R.id.txtName);
        TextView txtCuisines = (TextView) convertView.findViewById(R.id.txtCuisines);
        TextView txtCostForTwo = (TextView) convertView.findViewById(R.id.txtCostForTwo);
        TextView txtRating = (TextView) convertView.findViewById(R.id.txtRating);
        TextView txtAddress = (TextView) convertView.findViewById(R.id.txtAddress);

        // Populate the data into the template view using the data object
        txtName.setText(itemView.name);
        txtCuisines.setText(itemView.cuisines);
        txtCostForTwo.setText(itemView.costForTwo);
        txtRating.setText(itemView.rating);
        txtAddress.setText(itemView.address);

        // Return the completed view to render on screen
        return convertView;
    }
}
