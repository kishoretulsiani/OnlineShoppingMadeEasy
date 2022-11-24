package org.shopping.company.gateway.handler;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.google.common.base.CaseFormat;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import org.shopping.common.components.constants.ResponseStatus;
import org.shopping.common.components.constants.ResponseStatusMapper;
import org.shopping.common.components.constants.SorConstants;


public class ExceptionHandler implements Handler<RoutingContext> {

    private final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);
    private ResponseStatusMapper responseStatusMapper;

    public ExceptionHandler(ResponseStatusMapper responseStatusMapper) {
        this.responseStatusMapper = responseStatusMapper;
    }

    @Override
    public void handle(RoutingContext routingContext) {

        if (routingContext.failed()) {

            Throwable throwable = routingContext.failure();
            ResponseStatus responseStatus = responseStatusMapper.mapErrorCode(SorConstants.SYSTEM_ERROR);
            String statusCode = responseStatus.getStatusCode();
            String errorMessage = throwable.getMessage();

            if (throwable instanceof UnrecognizedPropertyException) {
                responseStatus = responseStatusMapper.mapErrorCode(SorConstants.INVALID_REQUEST_ERROR);
                UnrecognizedPropertyException unrecognizedPropertyException =
                        (UnrecognizedPropertyException) throwable.getCause();
                statusCode = "invalid_property_" + CaseFormat.UPPER_CAMEL
                        .to(CaseFormat.LOWER_UNDERSCORE, unrecognizedPropertyException.getPropertyName());
            }

            logger.error("Error occured in SOR API. Detailed Message : " + errorMessage, throwable, routingContext.get(SorConstants.TRACKING_ID));

            routingContext.response()
                    .setStatusCode(responseStatus.getHttpStatus())
                    .putHeader("Content-Type", "application/json")
                    .putHeader(SorConstants.TRACKING_ID, (String) routingContext.get(SorConstants.TRACKING_ID));
        }
    }
}
