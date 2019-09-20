package com.example.robertotarullo.myfridge.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.robertotarullo.myfridge.bean.PointOfPurchase;

import java.util.List;

public class PointsOfPurchaseSpinnerAdapter extends ArrayAdapter<PointOfPurchase> {
    private LayoutInflater inflater;
    private List<PointOfPurchase> pointsOfPurchase;

    public PointsOfPurchaseSpinnerAdapter(Context context, int resourceId, List<PointOfPurchase> pointsOfPurchase) {
        super(context, resourceId, pointsOfPurchase);
        this.inflater = LayoutInflater.from(context);
        this.pointsOfPurchase = pointsOfPurchase;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View v = super.getDropDownView(position, convertView, parent);
    		
        PointOfPurchase p = getItem(position);

        TextView tv = (TextView) v;
        //tv.setText(p.getName()); // prende automaticamente da toString() di PointOfPurchase
        if (position == 0)
            tv.setTextColor(Color.GRAY);
        else
            tv.setTextColor(Color.BLACK);
        return v;
    }
}

