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
import org.shopping.company.services.orders.steps.CreateOrderResponseStep;
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


        // Following steps will be executed in sequence to create an order for a customer

        // validate order received for logged in customer
        // validate create order request
        // update inventory of items in db and create order entry
        // calculate other order details tax and total amount and create order details in mongo
        // apply offers applicable
        // process payment - NOT IMPLEMENTED YET
        // create order response

        new ValidateLoggedInUserStep(databaseHelper, dtoHelper, redisHelper).execute(context)
                .thenCompose(new ValidateCreateOrderRequestStep(databaseHelper, dtoHelper, redisHelper)::execute)
                .thenCompose(new ConfirmInventoryStep(databaseHelper, dtoHelper, redisHelper)::execute)
                .thenCompose(new UpdateItemsInventoryStep(databaseHelper, dtoHelper, redisHelper)::execute)
                .thenCompose(new CalculateOrderAmountsStep(databaseHelper, dtoHelper, redisHelper)::execute)
                .thenCompose(new ApplyOffersStep(databaseHelper, dtoHelper, redisHelper)::execute)
                .thenCompose(new CreateOrderResponseStep(databaseHelper, dtoHelper, redisHelper)::execute)
                .thenCompose(new UpdateApplicationUserStep(databaseHelper, dtoHelper, redisHelper)::execute)
                .thenAccept(context1 -> {

                    context.setResponsePayload(Json.encode(context1.getCreateOrderResponse()));
                    ResponseStatus responseStatus = ResponseStatusMapper.getStatusCodeMap().get(ServiceAlerts.SUCCESS.getAlertCode());
                    context1.setResponseStatus(responseStatus);
                    logger.info("CreateOrderWorkflow Completed");
                    workflowFuture.complete(context1);

                }).exceptionally(throwable -> {
                    logger.error("Error occurred in executing CreateOrderWorkflow.");
                    //TODO good place to decide what we have to respond to user success or failure ???
                    // As we can still save everything from context and decided how to process later and eventually
                    // make the order successful wth various approaches
                    workflowFuture.completeExceptionally(throwable);
                    return null;
                });

        return workflowFuture;
    }


}
