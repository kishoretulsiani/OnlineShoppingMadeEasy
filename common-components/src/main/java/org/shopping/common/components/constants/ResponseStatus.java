package org.shopping.common.components.constants;


public class ResponseStatus {

    private final String statusCodeType;
    private final String statusCode;
    private final int httpStatus;

    public ResponseStatus(String statusCode, String statusCodeType, int httpStatus) {
        this.httpStatus = httpStatus;
        this.statusCode = statusCode;
        this.statusCodeType = statusCodeType;
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
}
