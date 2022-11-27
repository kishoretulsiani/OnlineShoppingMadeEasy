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
import org.shopping.company.services.orders.steps.ApplyOffersStep;
import org.shopping.company.services.orders.steps.CalculateOrderAmountsStep;
import org.shopping.company.services.orders.steps.ConfirmInventoryStep;
import org.shopping.company.services.orders.steps.CreateOrderStep;
import org.shopping.company.services.orders.steps.UpdateApplicationUserStep;
import org.shopping.company.services.orders.steps.UpdateItemsInventoryStep;
import org.shopping.company.services.orders.steps.ValidateCreateOrderRequestStep;
import org.shopping.company.services.orders.steps.ValidateLoggedInUserStep;

import java.util.concurrent.CompletableFuture;

public class CreateOrderWorkflow {

    private final Logger logger = LoggerFactory.getLogger(CreateOrderWorkflow.class);

    Context context = null;

    DatabaseHelper databaseHelper = new DatabaseHelper();

    DTOHelper dtoHelper = new DTOHelper();

    RedisHelper redisHelper = new RedisHelper();

    public CreateOrderWorkflow(Context context) {
        this.context = context;
    }

    public CompletableFuture<Context> execute() {

        logger.info("CreateOrderWorkflow Workflow Started");

        CompletableFuture<Context> workflowFuture = new CompletableFuture();


        // following steps will be executed in sequence to create an order for a customer

        // validate order received for logged in customer
        // validate create order request
        // update inventory of items in db and create order entry
        // calculate other order details tax and total amount and create order details in mongo
        // apply offers applicable
        // process payment
        // create order response

        new ValidateLoggedInUserStep(databaseHelper, dtoHelper, redisHelper).execute(context)
                .thenCompose(new ValidateCreateOrderRequestStep(databaseHelper, dtoHelper, redisHelper)::execute)
                .thenCompose(new ConfirmInventoryStep(databaseHelper, dtoHelper, redisHelper)::execute)
                .thenCompose(new UpdateItemsInventoryStep(databaseHelper, dtoHelper, redisHelper)::execute)
                .thenCompose(new CalculateOrderAmountsStep(databaseHelper, dtoHelper, redisHelper)::execute)
                .thenCompose(new ApplyOffersStep(databaseHelper, dtoHelper, redisHelper)::execute)
                .thenCompose(new CreateOrderStep(databaseHelper, dtoHelper, redisHelper)::execute)
                .thenCompose(new UpdateApplicationUserStep(databaseHelper, dtoHelper, redisHelper)::execute)
                .thenAccept(context1 -> {

                    context.setResponsePayload(Json.encode(context1.getCreateOrderResponse()));
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
