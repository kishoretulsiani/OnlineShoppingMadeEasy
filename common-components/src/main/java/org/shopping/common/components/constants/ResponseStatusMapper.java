package org.shopping.common.components.constants;


import java.util.HashMap;
import java.util.Map;


public class ResponseStatusMapper {


    private static final Map<Integer, ResponseStatus> statusCodeMap = new HashMap();

    public ResponseStatusMapper() {
        statusCodeMap.put(ServiceAlerts.SUCCESS.code, new ResponseStatus("not_eligible", "success", 200));
        statusCodeMap.put(ServiceAlerts.INTERNAL_ERROR.code, new ResponseStatus("system_error", "system_error", 500));
        statusCodeMap.put(ServiceAlerts.MANDATORY_DATA_MISSING.code, new ResponseStatus("mandatory_data_missing", "mandatory_data_missing", 400));
        statusCodeMap.put(ServiceAlerts.INVALID_USER.code, new ResponseStatus("not_authorized", "not_authorized", 401));
        statusCodeMap.put(ServiceAlerts.ITEM_OUT_OF_STOCK.code, new ResponseStatus("item_out_of_stock", "item_out_of_stock", 200));
        statusCodeMap.put(ServiceAlerts.NO_ORDERS_FOUND.code, new ResponseStatus("no_orders_found", "no_orders_found", 200));

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

}
