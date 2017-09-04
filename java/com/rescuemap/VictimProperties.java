package com.rescuemap;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by sam on 29.01.2017.
 */

public class VictimProperties {
    private int categoryIndex;
    private String name;
    private int id;
    private double speed_ms;
    LatLng latLng;
    Date date;

    private double km2m(double val_km) {
        return val_km/3.6;
    }

    public VictimProperties(String name, int id, int categoryIndex, LatLng latLng, Date date) {
        this.name = name;
        this.id = id;
        this.categoryIndex = categoryIndex;
        this.latLng = latLng;
        this.date = date;
        switch (categoryIndex) {
            case 0:
                this.speed_ms = km2m(MySettings.getChild7YearsOldSpeedKmph());
                break;
            case 1:
                this.speed_ms = km2m(MySettings.getChild15YearsOldSpeedKmph());
                break;
            case 2:
                this.speed_ms = km2m(MySettings.getAdult30YearsOldSpeedKmph());
                break;
            default:
                //TODO error message
        }
    }

    public int getID() {
        return id;
    }

    public int getCategoryIndex() {
        return categoryIndex;
    }

    public double getSpeed_mps() {
        return speed_ms;
    }

    public double getConstRadius_m() {
        double contRadius_m = Double.NaN;
        switch (categoryIndex) {
            case 0:
                contRadius_m = MySettings.getChild7YearsOldConstRadiusM();
                break;
            case 1:
                contRadius_m = MySettings.getChild15YearsOldConstRadiusM();
                break;
            case 2:
                contRadius_m = MySettings.getAdult30YearsOldConstRadiusM();
                break;
            default:
                //TODO error message
        }
        return contRadius_m;
    }

    public String getName() {return name;}

    public LatLng getLatLng() {
        return latLng;
    }

    public Date getDate() {
        return date;
    }
}
