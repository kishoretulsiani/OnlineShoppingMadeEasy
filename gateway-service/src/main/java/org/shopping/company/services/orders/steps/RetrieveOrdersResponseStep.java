package org.shopping.company.services.orders.steps;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.shopping.common.components.constants.ServiceAlerts;
import org.shopping.common.components.exception.ApplicationException;
import org.shopping.company.services.orders.helpers.DTOHelper;
import org.shopping.company.services.orders.helpers.DatabaseHelper;
import org.shopping.company.services.orders.helpers.RedisHelper;
import org.shopping.company.services.orders.response.RetrieveOrdersResponse;
import org.shopping.company.services.orders.workflow.Context;
import org.shopping.company.services.orders.workflow.WorkflowStep;

import java.util.concurrent.CompletableFuture;

public class RetrieveOrdersResponseStep implements WorkflowStep {

    private final Logger logger = LoggerFactory.getLogger(RetrieveOrdersResponseStep.class);

    DatabaseHelper databaseHelper;

    DTOHelper dtoHelper;

    RedisHelper redisHelper;

    public RetrieveOrdersResponseStep(DatabaseHelper databaseHelper, DTOHelper dtoHelper, RedisHelper redisHelper) {
        this.databaseHelper = databaseHelper;
        this.dtoHelper = dtoHelper;
        this.redisHelper = redisHelper;
    }

    @Override
    public CompletableFuture<Context> execute(Context context) {
        logger.info("Workflow RetrieveOrdersResponseStep Executed");
        CompletableFuture<Context> retrieveOrdersResponseStepFuture = new CompletableFuture();

        RetrieveOrdersResponse retrieveOrdersResponse = new RetrieveOrdersResponse();

        if (context.getOrders() != null && context.getOrders().size() > 0) {
            retrieveOrdersResponse.setOrders(context.getOrders());
            context.setRetrieveOrdersResponse(retrieveOrdersResponse);
            retrieveOrdersResponseStepFuture.complete(context);
        } else {
            retrieveOrdersResponseStepFuture.completeExceptionally(new ApplicationException(ServiceAlerts.NO_ORDERS_FOUND.getAlertCode(), ServiceAlerts.NO_ORDERS_FOUND.getAlertMessage(), null));
        }

        return retrieveOrdersResponseStepFuture;
    }
}
