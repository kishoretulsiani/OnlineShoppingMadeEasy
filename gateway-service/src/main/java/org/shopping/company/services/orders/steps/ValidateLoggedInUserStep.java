package org.shopping.company.services.orders.steps;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.shopping.common.components.constants.ServiceAlerts;
import org.shopping.common.components.exception.ApplicationException;
import org.shopping.common.components.utils.JsonUtility;
import org.shopping.company.services.orders.helpers.DTOHelper;
import org.shopping.company.services.orders.helpers.DatabaseHelper;
import org.shopping.company.services.orders.helpers.RedisHelper;
import org.shopping.company.services.orders.request.CreateOrderRequest;
import org.shopping.company.services.orders.workflow.Context;
import org.shopping.company.services.orders.workflow.WorkflowStep;

import java.util.concurrent.CompletableFuture;

public class ValidateLoggedInUserStep implements WorkflowStep {

    private final Logger logger = LoggerFactory.getLogger(ValidateLoggedInUserStep.class);

    DatabaseHelper databaseHelper;

    DTOHelper dtoHelper;

    RedisHelper redisHelper;

    public ValidateLoggedInUserStep(DatabaseHelper databaseHelper, DTOHelper dtoHelper, RedisHelper redisHelper) {
        this.databaseHelper = databaseHelper;
        this.dtoHelper = dtoHelper;
        this.redisHelper = redisHelper;
    }


    @Override
    public CompletableFuture<Context> execute(Context context) {
        logger.info("Workflow ValidateLoggedInUserStep Executed");

        CompletableFuture<Context> validateLoggedInUserStepFuture = new CompletableFuture();

        String strRequest = context.getRequestObject().toString();

        if (StringUtils.isNotBlank(strRequest) && !strRequest.equals("{}")) {
            final CreateOrderRequest createOrderRequest = JsonUtility.getInstance().getObject(strRequest, CreateOrderRequest.class);
            logger.info("Request got converted to object");
            context.setCreateOrderRequest(createOrderRequest);

            String requestUserId = createOrderRequest.getUserId();

            databaseHelper.getApplicationUser(requestUserId).thenAccept(user -> {
                if (user != null) {
                    context.setLoggedInUser(user);
                    validateLoggedInUserStepFuture.complete(context);
                } else {
                    validateLoggedInUserStepFuture.completeExceptionally(new ApplicationException(ServiceAlerts.INVALID_USER.getAlertCode(), ServiceAlerts.INVALID_USER.getAlertMessage(), null));
                }
            }).exceptionally(throwable -> {
                validateLoggedInUserStepFuture.completeExceptionally(new ApplicationException(ServiceAlerts.INVALID_USER.getAlertCode(), ServiceAlerts.INVALID_USER.getAlertMessage(), null));
                return null;
            });

        } else {
            logger.info("Userid in request is invalid");
            validateLoggedInUserStepFuture.completeExceptionally(new ApplicationException(ServiceAlerts.INVALID_USER.getAlertCode(), ServiceAlerts.INVALID_USER.getAlertMessage(), null));
        }

        return validateLoggedInUserStepFuture;
    }
}
