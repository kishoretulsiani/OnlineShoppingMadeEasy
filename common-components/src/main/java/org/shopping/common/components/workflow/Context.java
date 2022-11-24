package org.shopping.common.components.workflow;


import io.vertx.core.json.JsonObject;

public class Context {
    private JsonObject requestObject;
    private String responsePayload;
    private String serviceName;
    private String trackingId;

    public JsonObject getRequestObject() {
        return requestObject;
    }

    public Context setRequestObject(JsonObject requestObject) {
        this.requestObject = requestObject;
        return this;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public Context setResponsePayload(String responsePayload) {
        this.responsePayload = responsePayload;
        return this;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Context setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public Context setTrackingId(String trackingId) {
        this.trackingId = trackingId;
        return this;
    }
}
