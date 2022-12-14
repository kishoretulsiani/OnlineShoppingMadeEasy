

package org.shopping.common.components.exception;


import org.apache.commons.lang3.exception.ExceptionUtils;

public class ApplicationException extends RuntimeException {
    private final int responseCode;
    private final String responseMsg;
    private final String errorCause;
    private final String[] errorArgs;

    public ApplicationException(int responseCode, String responseMsg, Throwable errorCause) {
        super(responseMsg, errorCause);
        this.errorArgs = null;
        this.responseCode = responseCode;
        this.responseMsg = responseMsg;
        if (errorCause != null) {
            this.errorCause = ExceptionUtils.getStackTrace(errorCause);
        } else {
            this.errorCause = null;
        }

    }

    public String[] getErrorArgs() {
        return this.errorArgs;
    }

    public String getErrorCause() {
        return this.errorCause;
    }

    public int getResponseCode() {
        return this.responseCode;
    }

    public String getResponseMsg() {
        return this.responseMsg;
    }
}
