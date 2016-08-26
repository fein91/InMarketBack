package com.fein91.rest.exception;

import com.fein91.model.OrderResult;

public class RollbackOnCalculateException extends RuntimeException {
    final OrderResult orderResult;

    public RollbackOnCalculateException(OrderResult orderResult) {
        this.orderResult = orderResult;
    }

    public RollbackOnCalculateException(String message, OrderResult orderResult) {
        super(message);
        this.orderResult = orderResult;
    }

    public OrderResult getOrderResult() {
        return orderResult;
    }
}
