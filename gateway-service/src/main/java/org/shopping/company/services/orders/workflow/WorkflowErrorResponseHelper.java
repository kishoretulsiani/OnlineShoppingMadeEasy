package org.shopping.company.services.orders.workflow;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.shopping.common.components.constants.ServiceAlerts;
import org.shopping.common.components.exception.ApplicationException;

import java.util.Optional;

public class WorkflowErrorResponseHelper {

    private final static Logger logger = LoggerFactory.getLogger(WorkflowErrorResponseHelper.class);

    public static JsonObject getErrorDetails(Throwable throwable, Context context) {

        logger.info("ErrorResponseHelper creating error response");

        String detailedErrorTrace = Optional.ofNullable(throwable).isPresent() ? throwable.getStackTrace().toString() : "";

        int responseCode;
        String responseMessage;
        if (throwable.getCause() instanceof ApplicationException) {
            ApplicationException applicationException = (ApplicationException) throwable.getCause();
            responseCode = applicationException.getResponseCode();
            responseMessage = applicationException.getResponseMsg();
        } else {
            responseCode = ServiceAlerts.INTERNAL_ERROR.getAlertCode();
            responseMessage = ServiceAlerts.INTERNAL_ERROR.getAlertMessage();
        }

        JsonObject errorResponse = new JsonObject();
        errorResponse.put("status_code", responseCode);
        errorResponse.put("status_code_type", responseMessage);
        errorResponse.put("detailed_error_trace", detailedErrorTrace);


        return errorResponse;

    }
}
