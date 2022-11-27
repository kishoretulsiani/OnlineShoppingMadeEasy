package org.shopping.company.services.orders.workflow;

import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.shopping.common.components.constants.ResponseStatus;
import org.shopping.common.components.constants.ResponseStatusMapper;
import org.shopping.common.components.constants.ServiceAlerts;
import org.shopping.company.services.orders.helpers.DTOHelper;
import org.shopping.company.services.orders.helpers.DatabaseHelper;
import org.shopping.company.services.orders.helpers.RedisHelper;
import org.shopping.company.services.orders.steps.RetrieveOrderIdsForUserStep;
import org.shopping.company.services.orders.steps.RetrieveOrdersForUserStep;
import org.shopping.company.services.orders.steps.RetrieveOrdersResponseStep;

import java.util.concurrent.CompletableFuture;

public class RetrieveOrdersWorkflow {

    private final Logger logger = LoggerFactory.getLogger(RetrieveOrdersWorkflow.class);

    Context context = null;

    DatabaseHelper databaseHelper = new DatabaseHelper();

    DTOHelper dtoHelper = new DTOHelper();

    RedisHelper redisHelper = new RedisHelper();

    public RetrieveOrdersWorkflow(Context context) {
        this.context = context;
    }

    public CompletableFuture<Context> execute() {

        logger.info("RetrieveOrdersWorkflow Workflow Started");

        CompletableFuture<Context> workflowFuture = new CompletableFuture();


        // following steps will be executed in sequence to retrieve orders

        // validate logged in customer
        // retrieve order ids for user
        // retrieve order details
        // create response


        new RetrieveOrderIdsForUserStep(databaseHelper, dtoHelper, redisHelper).execute(context)
                .thenCompose(new RetrieveOrdersForUserStep(databaseHelper, dtoHelper, redisHelper)::execute)
                .thenCompose(new RetrieveOrdersResponseStep(databaseHelper, dtoHelper, redisHelper)::execute)
                .thenAccept(context1 -> {
                    context.setResponsePayload(Json.encode(context1.getRetrieveOrdersResponse()));
                    logger.info("Workflow Completed");
                    ResponseStatus responseStatus = ResponseStatusMapper.getStatusCodeMap().get(ServiceAlerts.SUCCESS.getAlertCode());
                    context1.setResponseStatus(responseStatus);
                    workflowFuture.complete(context1);

                }).exceptionally(throwable -> {
                    logger.error("Error occurred in executing workflow.");
                    workflowFuture.completeExceptionally(throwable);
                    return null;
                });

        return workflowFuture;
    }


}
