package org.shopping.company.services.orders.steps;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.shopping.common.components.constants.ServiceAlerts;
import org.shopping.common.components.exception.ApplicationException;
import org.shopping.common.components.mongo.MongoDB;
import org.shopping.common.components.utils.JsonUtility;
import org.shopping.company.services.orders.request.CreateOrderRequest;
import org.shopping.company.services.orders.workflow.Context;

import java.util.concurrent.CompletableFuture;

public class ValidateLoggedInUserStep implements WorkflowStep {

    private final Logger logger = LoggerFactory.getLogger(ValidateLoggedInUserStep.class);


    @Override
    public CompletableFuture<Context> execute(Context context) {
        logger.info("Workflow ValidateLoggedInUserStep Executed");

        CompletableFuture<Context> validateLoggedInUserStepFuture = new CompletableFuture();

        String strRequest = context.getRequestObject().toString();

        if (StringUtils.isNotBlank(strRequest)) {
            final CreateOrderRequest createOrderRequest = JsonUtility.getInstance().getObject(strRequest, CreateOrderRequest.class);
            logger.info("Request got converted to object");
            context.setCreateOrderRequest(createOrderRequest);

            String requestUserId = createOrderRequest.getUserId();
            JsonObject filter = new JsonObject().put("userId", requestUserId);

            MongoDB.getClient().find("APPLICATION_USERS", filter, result -> {
                if (result.succeeded() && result.result().size() > 0) {
                    logger.info("Userid in request successfully validated");
                    validateLoggedInUserStepFuture.complete(context);
                } else {
                    logger.info("Userid in request is invalid");
                    validateLoggedInUserStepFuture.completeExceptionally(new ApplicationException(ServiceAlerts.INVALID_USER.getAlertCode(), ServiceAlerts.INVALID_USER.getAlertMessage(), null));
                }
            });

        } else {
            logger.info("Userid in request is invalid");
            validateLoggedInUserStepFuture.completeExceptionally(new ApplicationException(ServiceAlerts.INVALID_USER.getAlertCode(), ServiceAlerts.INVALID_USER.getAlertMessage(), null));
        }

        return validateLoggedInUserStepFuture;
    }
}
