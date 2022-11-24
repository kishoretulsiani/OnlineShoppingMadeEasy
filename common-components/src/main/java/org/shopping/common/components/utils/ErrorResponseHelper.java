package org.shopping.common.components.utils;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.shopping.common.components.constants.ResponseStatus;
import org.shopping.common.components.constants.ResponseStatusMapper;
import org.shopping.common.components.constants.ServiceAlerts;
import org.shopping.common.components.constants.SorConstants;
import org.shopping.common.components.workflow.Context;

import java.util.Optional;

public class ErrorResponseHelper {

    private final Logger logger = LoggerFactory.getLogger(ErrorResponseHelper.class);


    public static JsonObject getErrorDetails(Throwable throwable, Context context) {

        String detailedErrorTrace = Optional.ofNullable(throwable).isPresent() ? throwable.getStackTrace().toString() : "";

        ResponseStatus responseStatus = ResponseStatusMapper.getStatusCodeMap().get(ServiceAlerts.INTERNAL_API_ERROR.getAlertCode());

        String statusCode = responseStatus != null ? responseStatus.getStatusCode() : "";

        JsonObject errorResponse = new JsonObject();
        errorResponse.put("status_code", statusCode);
        errorResponse.put("status_code_type", responseStatus.getStatusCodeType());
        errorResponse.put("detailed_error_trace", detailedErrorTrace);
        JsonObject jsonObject = new JsonObject();
        jsonObject.put(SorConstants.TRACKING_ID, context.getTrackingId());
        jsonObject.put("error_details", errorResponse);
        return jsonObject;

    }
}
