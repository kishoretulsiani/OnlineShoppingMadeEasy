package org.shopping.common.components.constants;

public enum Status {
    SYSTEM_EXCEPTION("Failure"),
    BUSINESS_EXCEPTION("BusinessException"),
    USER_EXCEPTION("UserException"),
    SUCCESS("Success"),
    TIMEOUT("Timeout");

    private final String desc;

    private Status(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return this.desc;
    }

    public String toString() {
        return this.desc;
    }
}
