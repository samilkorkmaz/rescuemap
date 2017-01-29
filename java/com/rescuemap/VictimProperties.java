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
        this.speed_ms = (categoryIndex + 1) * 100;
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
