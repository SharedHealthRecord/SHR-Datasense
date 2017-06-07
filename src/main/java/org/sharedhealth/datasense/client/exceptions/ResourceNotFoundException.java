package org.sharedhealth.datasense.client.exceptions;

public class ResourceNotFoundException extends RuntimeException {

    private final int errorCode;

    public ResourceNotFoundException(String message, int errorCode, Throwable e) {
        super(message, e);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
