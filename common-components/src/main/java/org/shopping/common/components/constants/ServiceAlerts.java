package org.shopping.common.components.constants;


public enum ServiceAlerts {

    SUCCESS(0000, "SUCCESS"),
    INVALID_USER(4000, "DECLINED"),
    MANDATORY_DATA_MISSING(1005, "Mandatory Data Missing from "),
    INTERNAL_ERROR(1002, "Internal API error occurred in "),
    ITEM_OUT_OF_STOCK(1003, "Item is out of stock ");

    int code;
    String message;

    private ServiceAlerts(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getAlertCode() {
        return this.code;
    }

    public String getAlertMessage() {
        return this.message;
    }
}
