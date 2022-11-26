package org.shopping.company.gateway.handler;

import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.serviceproxy.ServiceException;
import org.shopping.common.components.constants.ResponseStatus;
import org.shopping.common.components.constants.ResponseStatusMapper;
import org.shopping.common.components.constants.ServiceAlerts;
import org.shopping.common.components.exception.ApplicationException;
import org.shopping.company.gateway.constants.GatewayConstant;


public class ExceptionHandler implements Handler<RoutingContext> {

    private final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);
    private ResponseStatusMapper responseStatusMapper;

    public ExceptionHandler(ResponseStatusMapper responseStatusMapper) {
        this.responseStatusMapper = responseStatusMapper;
    }

    @Override
    public void handle(RoutingContext routingContext) {

        ResponseStatus responseStatus;

        int httpStatusCode;

        Throwable throwable = routingContext.failure();

        if (throwable instanceof ServiceException) {
            logger.error("ApplicationException occurred  " + throwable.getMessage());
            ServiceException exception = (ServiceException) throwable;
            JsonObject errorInfo = exception.getDebugInfo();
            responseStatus = responseStatusMapper.mapErrorCode(errorInfo.getInteger("status_code"));
        } else if (throwable instanceof ApplicationException) {
            logger.error("ApplicationException occurred  " + throwable.getMessage());
            ApplicationException exception = (ApplicationException) throwable;
            responseStatus = responseStatusMapper.mapErrorCode(exception.getResponseCode());
        } else {
            logger.error("Some Exception occurred  " + throwable.getMessage());
            responseStatus = responseStatusMapper.mapErrorCode(ServiceAlerts.INTERNAL_ERROR.getAlertCode());
        }

        httpStatusCode = responseStatus.getHttpStatus();

        JsonObject jsonObject = new JsonObject();
        jsonObject.put("status_code", responseStatus.getStatusCode());
        jsonObject.put("status_code_type", responseStatus.getStatusCodeType());

        routingContext.response()
                .setStatusCode(httpStatusCode)
                .putHeader("Content-Type", "application/json")
                .putHeader(GatewayConstant.TRACKING_ID, (String) routingContext.get(GatewayConstant.TRACKING_ID))
                .end(Json.encode(jsonObject));

    }
}
