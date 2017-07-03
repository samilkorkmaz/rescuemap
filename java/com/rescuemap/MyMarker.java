package com.rescuemap;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.Marker;

import java.util.Date;

/**
 * Created by sam on 3.7.2017.
 */

public class MyMarker {
    private Marker googleMapMarker;

    public Marker getMarker() {
        return googleMapMarker;
    }

    public Circle getCircle() {
        return googleMapCircle;
    }

    public void setGoogleMapCircle(Circle googleMapCircle) {
        this.googleMapCircle = googleMapCircle;
    }

    public double getPrevRadius_m() {
        return prevRadius_m;
    }

    public void setPrevRadius_m(double prevRadius_m) {
        this.prevRadius_m = prevRadius_m;
    }

    public Date getPrevTime_ms() {
        return prevTime_ms;
    }

    public void setPrevTime_ms(Date prevTime_ms) {
        this.prevTime_ms = prevTime_ms;
    }

    private Circle googleMapCircle;
    private double prevRadius_m;
    private Date prevTime_ms;

    public MyMarker(Marker googleMapMarker, Circle googleMapCircle, Date date) {
        this.googleMapMarker = googleMapMarker;
        this.googleMapCircle = googleMapCircle;
        prevRadius_m = 0;
        prevTime_ms = date;
    }

    public Object getTag() {
        return googleMapMarker.getTag();
    }

    public void remove() {
        googleMapCircle.remove();
        googleMapMarker.remove();
    }



}
