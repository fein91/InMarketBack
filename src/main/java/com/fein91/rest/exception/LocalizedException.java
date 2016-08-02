package com.fein91.rest.exception;

public abstract class LocalizedException extends RuntimeException {

    private String localizedMsg;

    public LocalizedException(String message, String localizedMsg) {
        super(message);
        this.localizedMsg = localizedMsg;
    }

    public LocalizedException(String message, String localizedMsg, Throwable cause) {
        super(message, cause);
        this.localizedMsg = localizedMsg;
    }

    public String getLocalizedMsg() {
        return localizedMsg;
    }

    public void setLocalizedMsg(String localizedMsg) {
        this.localizedMsg = localizedMsg;
    }
}
