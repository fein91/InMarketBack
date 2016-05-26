package com.fein91.core.model;

/**
 * Created by fein on 5/23/2016.
 */
public enum OrderType {
    ASK("offer", 0),
    BID("bid", 1);

    final String coreName;
    final int id;

    OrderType(String coreName, int id) {
        this.coreName = coreName;
        this.id = id;
    }

    public String getCoreName() {
        return coreName;
    }

    public int getId() {
        return id;
    }

    public static OrderType valueOf(int id) {
        for (OrderType orderType : OrderType.values()) {
            if (orderType.id == id) {
                return orderType;
            }
        }
        throw new IllegalArgumentException("Unknown order type id: " + id);
    }
}
