package org.shopping.common.components.constants;


import java.util.HashMap;
import java.util.Map;


public class ResponseStatusMapper {


    private static final Map<Integer, ResponseStatus> statusCodeMap = new HashMap();

    public ResponseStatusMapper() {
        statusCodeMap.put(1200, new ResponseStatus("not_eligible", "success", 200, Status.BUSINESS_EXCEPTION));
        statusCodeMap.put(1002, new ResponseStatus("system_error", "error", 500, Status.SYSTEM_EXCEPTION));
    }

    public static Map<Integer, ResponseStatus> getStatusCodeMap() {
        return statusCodeMap;
    }

    public ResponseStatus mapErrorCode(int errorCode) {
        ResponseStatus responseStatus = statusCodeMap.get(errorCode);
        if (responseStatus == null) {
            return statusCodeMap.get(1002);
        }
        return responseStatus;
    }

    private int getHttpStatus(String status) {
        return Integer.valueOf(status);
    }

}
