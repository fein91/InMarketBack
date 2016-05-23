package com.fein91.core.model;

/**
 * Created by fein on 5/23/2016.
 */
public enum OrderType {
    ASK("offer"),
    BID("bid");

    final String coreName;

    OrderType(String coreName) {
        this.coreName = coreName;
    }

    public String getCoreName() {
        return coreName;
    }
}
