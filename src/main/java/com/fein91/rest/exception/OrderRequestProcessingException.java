package com.fein91.rest.exception;

/**
 * Unchecked exception for order processing errors
 */
public class OrderRequestProcessingException extends RuntimeException {

    public OrderRequestProcessingException(String errorMessage) {
        super(errorMessage);
    }
    public OrderRequestProcessingException() {
        super();
    }
}
