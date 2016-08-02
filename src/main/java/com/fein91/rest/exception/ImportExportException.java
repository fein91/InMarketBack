package com.fein91.rest.exception;

public class ImportExportException extends LocalizedException {

    public ImportExportException(String message, String localizedMessage) {
        super(message, localizedMessage);
    }

    public ImportExportException(String message, String localizedMessage, Throwable cause) {
        super(message, localizedMessage, cause);
    }
}
