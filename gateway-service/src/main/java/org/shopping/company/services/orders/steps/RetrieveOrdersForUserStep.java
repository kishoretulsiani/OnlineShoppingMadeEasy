package org.shopping.company.services.orders.steps;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.shopping.common.components.constants.ServiceAlerts;
import org.shopping.common.components.exception.ApplicationException;
import org.shopping.company.services.orders.helpers.DTOHelper;
import org.shopping.company.services.orders.helpers.DatabaseHelper;
import org.shopping.company.services.orders.helpers.RedisHelper;
import org.shopping.company.services.orders.workflow.Context;
import org.shopping.company.services.orders.workflow.WorkflowStep;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RetrieveOrdersForUserStep implements WorkflowStep {

    private final Logger logger = LoggerFactory.getLogger(RetrieveOrdersForUserStep.class);

    DatabaseHelper databaseHelper;

    DTOHelper dtoHelper;

    RedisHelper redisHelper;

    public RetrieveOrdersForUserStep(DatabaseHelper databaseHelper, DTOHelper dtoHelper, RedisHelper redisHelper) {
        this.databaseHelper = databaseHelper;
        this.dtoHelper = dtoHelper;
        this.redisHelper = redisHelper;
    }


    @Override
    public CompletableFuture<Context> execute(Context context) {
        logger.info("Workflow RetrieveOrdersForUserStep Executed");

        CompletableFuture<Context> retrieveOrdersForUserStepFuture = new CompletableFuture();

        List<String> orderIds = context.getUserOrderIds();

        if (orderIds != null && orderIds.size() > 0) {

            List<String> requestOrderIds = context.getRetrieveOrdersRequest().getOrderIds();

            if (orderIds.containsAll(requestOrderIds)) {
                // dirty way to return only what is asked in request :)
                orderIds = requestOrderIds;
            } else {
                // return everything
                //TODO will implement later :)
            }

            databaseHelper.getUserOrders(orderIds).thenAccept(orders -> {
                if (orders.size() > 0) {
                    context.setOrders(orders);
                    retrieveOrdersForUserStepFuture.complete(context);
                } else {
                    retrieveOrdersForUserStepFuture.completeExceptionally(new ApplicationException(ServiceAlerts.NO_ORDERS_FOUND.getAlertCode(), ServiceAlerts.NO_ORDERS_FOUND.getAlertMessage(), null));
                }
            });
        } else {
            retrieveOrdersForUserStepFuture.completeExceptionally(new ApplicationException(ServiceAlerts.NO_ORDERS_FOUND.getAlertCode(), ServiceAlerts.NO_ORDERS_FOUND.getAlertMessage(), null));
        }


        return retrieveOrdersForUserStepFuture;
    }
}
