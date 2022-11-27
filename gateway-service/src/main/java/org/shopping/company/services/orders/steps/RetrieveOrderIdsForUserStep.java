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
import org.shopping.company.services.orders.request.RetrieveOrdersRequest;
import org.shopping.company.services.orders.workflow.Context;
import org.shopping.company.services.orders.workflow.WorkflowStep;

import java.util.concurrent.CompletableFuture;

public class RetrieveOrderIdsForUserStep implements WorkflowStep {

    private final Logger logger = LoggerFactory.getLogger(RetrieveOrderIdsForUserStep.class);

    DatabaseHelper databaseHelper;

    DTOHelper dtoHelper;

    RedisHelper redisHelper;

    public RetrieveOrderIdsForUserStep(DatabaseHelper databaseHelper, DTOHelper dtoHelper, RedisHelper redisHelper) {
        this.databaseHelper = databaseHelper;
        this.dtoHelper = dtoHelper;
        this.redisHelper = redisHelper;
    }


    @Override
    public CompletableFuture<Context> execute(Context context) {
        logger.info("Workflow ValidateLoggedInUserStep Executed");

        CompletableFuture<Context> retrieveOrderIdsForUserStepFuture = new CompletableFuture();

        String strRequest = context.getRequestObject().toString();

        if (StringUtils.isNotBlank(strRequest) && !strRequest.equals("{}")) {
            final RetrieveOrdersRequest retrieveOrdersRequest = JsonUtility.getInstance().getObject(strRequest, RetrieveOrdersRequest.class);
            logger.info("Request got converted to object");
            context.setRetrieveOrdersRequest(retrieveOrdersRequest);

            String requestUserId = retrieveOrdersRequest.getUserId();

            databaseHelper.getUserOrderIds(requestUserId).thenAccept(userOrderIds -> {
                if (userOrderIds.size() > 0) {
                    context.setUserOrderIds(userOrderIds);
                    retrieveOrderIdsForUserStepFuture.complete(context);
                } else {
                    retrieveOrderIdsForUserStepFuture.completeExceptionally(new ApplicationException(ServiceAlerts.NO_ORDERS_FOUND.getAlertCode(), ServiceAlerts.NO_ORDERS_FOUND.getAlertMessage(), null));
                }
            }).exceptionally(throwable -> {
                retrieveOrderIdsForUserStepFuture.completeExceptionally(new ApplicationException(ServiceAlerts.NO_ORDERS_FOUND.getAlertCode(), ServiceAlerts.NO_ORDERS_FOUND.getAlertMessage(), null));
                return null;
            });

        } else {
            logger.info("Userid in request is invalid");
            retrieveOrderIdsForUserStepFuture.completeExceptionally(new ApplicationException(ServiceAlerts.INVALID_USER.getAlertCode(), ServiceAlerts.INVALID_USER.getAlertMessage(), null));
        }

        return retrieveOrderIdsForUserStepFuture;
    }
}
