package com.fein91.rest.exception;

/**
 * Checked exception for user input order request  validation
 */
public class OrderRequestException extends Exception {

    public OrderRequestException(String errorMessage) {
        super(errorMessage);
    }
    public OrderRequestException() {
        super();
    }
}
