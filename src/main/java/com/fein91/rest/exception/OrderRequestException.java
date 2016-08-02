package com.fein91.rest.exception;

/**
 * Checked exception for user input order request  validation
 */
public class OrderRequestException extends LocalizedException {

    public OrderRequestException(String message, String localizedMessage, Throwable cause) {
        super(message, localizedMessage, cause);
    }

    public OrderRequestException(String message, String localizedMessage) {
        super(message, localizedMessage);
    }
}
