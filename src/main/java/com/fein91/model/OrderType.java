package com.fein91.model;

/**
 * Created by olta1014 on 27.05.2016.
 */
public enum OrderType {
    LIMIT(0),
    MARKET(1);

    final int id;

    OrderType(int id) {
        this.id = id;
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
