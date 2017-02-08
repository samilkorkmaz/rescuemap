package com.rescuemap;

/**
 * Created by sam on 29.01.2017.
 */

public class VictimProperties {
    private int categoryIndex;
    private int id;
    private double speed_ms;

    public VictimProperties(int id, int categoryIndex) {
        this.id = id;
        this.categoryIndex = categoryIndex;
        switch (categoryIndex) {
            case 0:
                this.speed_ms = MySettings.getChild7YearsOldSpeedMps();
                break;
            case 1:
                this.speed_ms = MySettings.getChild15YearsOldSpeedMps();
                break;
            case 2:
                this.speed_ms = MySettings.getAdult30YearsOldSpeedMps();
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
