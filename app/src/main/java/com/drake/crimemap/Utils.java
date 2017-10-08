package com.drake.crimemap;


import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.util.List;

public class Utils {

    private static final int COLOR_BLACK_ARGB = 0xff0000ff;
    private static final int COLOR_PRIMARY_ARGB = 0xff3F51B5;

    private static final int POLYLINE_STROKE_WIDTH_PX = 12;

    public static void stylePolyline(Polyline polyline) {
//        String type = "";
        // Get the data object stored with the polyline.
//        if (polyline.getTag() != null) {
//            type = polyline.getTag().toString();
//        }

//        switch (type) {
//            // If no type is given, allow the API to use the default.
//            case "A":
//                // Use a custom bitmap as the cap at the start of the line.
//                polyline.setStartCap(
//                        new CustomCap(
//                                BitmapDescriptorFactory.fromResource(R.drawable.my_arrow), 10));
//                break;
//            case "B":
//                // Use a round cap at the start of the line.
//                polyline.setStartCap(new RoundCap());
//                break;
//        }

        polyline.setEndCap(new RoundCap());
        polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
        polyline.setColor(COLOR_PRIMARY_ARGB);
        polyline.setJointType(JointType.ROUND);
    }


    public static PolylineOptions constructPolyline(List<LatLng> latLngList) {
        PolylineOptions polylineOptions = new PolylineOptions().clickable(false);

        for (LatLng latLng : latLngList) {
            polylineOptions.add(latLng);
        }

        return polylineOptions;
    }


    public static void addCircle(GoogleMap map, LatLng latLng, int color) {
        Circle circle = map.addCircle(new CircleOptions()
                .center(latLng)
                .radius(150)
                .strokeWidth(0)
//                .strokeColor(Color.GREEN)
//                .fillColor(Color.argb(128, 255, 0, 0))
                        .fillColor(color)
        );
    }

    public static void addCircles(List<LatLng> coordsList, GoogleMap map) {
        for (LatLng latLng : coordsList) {
            addCircle(map, latLng, Color.GREEN);
        }
    }
}
