package com.fein91.rest.exception;

/**
 * Unchecked exception for order processing errors
 */
public class OrderRequestProcessingException extends LocalizedException {


    public OrderRequestProcessingException(String message, String localizedMessage) {
        super(message, localizedMessage);
    }

    public OrderRequestProcessingException(String message, String localizedMessage, Throwable cause) {
        super(message, localizedMessage, cause);
    }
}
