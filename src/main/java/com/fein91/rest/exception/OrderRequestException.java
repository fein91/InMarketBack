package com.fein91.rest.exception;

public class OrderRequestException extends Exception {

    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }
    public OrderRequestException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }
    public OrderRequestException() {
        super();
    }
}
