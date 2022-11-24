package org.shopping.common.components.constants;


public class ResponseStatus {

    private final String statusCodeType;
    private final String statusCode;
    private final int httpStatus;
    private final Status status;

    public ResponseStatus(String statusCode, String statusCodeType, int httpStatus, Status status) {
        this.httpStatus = httpStatus;
        this.statusCode = statusCode;
        this.statusCodeType = statusCodeType;
        this.status = status;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getStatusCodeType() {
        return statusCodeType;
    }

    public Status getStatus() {
        return status;
    }
}
