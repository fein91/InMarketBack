package com.fein91.model;

/**
 * Created by olta1014 on 23.05.2016.
 */
public enum OrderType {
    ASK(0),
    BID(1);

    int id;

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
