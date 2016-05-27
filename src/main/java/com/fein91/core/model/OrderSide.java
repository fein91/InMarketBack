package com.fein91.core.model;

/**
 * Created by fein on 5/23/2016.
 */
public enum OrderSide {
    ASK("offer", 0),
    BID("bid", 1);

    final String coreName;
    final int id;

    OrderSide(String coreName, int id) {
        this.coreName = coreName;
        this.id = id;
    }

    public String getCoreName() {
        return coreName;
    }

    public int getId() {
        return id;
    }

    public static OrderSide valueOf(int id) {
        for (OrderSide orderSide : OrderSide.values()) {
            if (orderSide.id == id) {
                return orderSide;
            }
        }
        throw new IllegalArgumentException("Unknown order side id: " + id);
    }
}
