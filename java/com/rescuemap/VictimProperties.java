package com.rescuemap;

/**
 * Created by sam on 29.01.2017.
 */

public class VictimProperties {
    private int categoryIndex;
    private int id;
    private double speed_ms;

    private double km2m(double val_km) {
        return val_km/3.6;
    }

    public VictimProperties(int id, int categoryIndex) {
        this.id = id;
        this.categoryIndex = categoryIndex;
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
}
