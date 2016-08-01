package com.fein91.rest.exception;

public abstract class LocalizedException extends RuntimeException {

    private String localizedMessage;

    public LocalizedException(String message, String localizedMessage) {
        super(message);
        this.localizedMessage = localizedMessage;
    }

    public LocalizedException(String message, String localizedMessage, Throwable cause) {
        super(message, cause);
        this.localizedMessage = localizedMessage;
    }

    @Override
    public String getLocalizedMessage() {
        return localizedMessage;
    }

    public void setLocalizedMessage(String localizedMessage) {
        this.localizedMessage = localizedMessage;
    }
}
