package org.shopping.company.services.orders.workflow;

import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.shopping.common.components.constants.ResponseStatus;
import org.shopping.common.components.constants.ResponseStatusMapper;
import org.shopping.common.components.constants.ServiceAlerts;
import org.shopping.company.services.orders.response.CreateOrderResponse;
import org.shopping.company.services.orders.steps.CalculateOrderAmountsStep;
import org.shopping.company.services.orders.steps.ConfirmAndUpdateItemsInventoryStep;
import org.shopping.company.services.orders.steps.CreateOrderStep;
import org.shopping.company.services.orders.steps.ValidateCreateOrderRequestStep;
import org.shopping.company.services.orders.steps.ValidateLoggedInUserStep;

import java.util.concurrent.CompletableFuture;

public class CreateOrderWorkflow {

    private final Logger logger = LoggerFactory.getLogger(CreateOrderWorkflow.class);

    Context context = null;

    public CreateOrderWorkflow(Context context) {
        this.context = context;
    }

    public CompletableFuture<Context> execute() {

        logger.info("Workflow Started");

        CompletableFuture<Context> workflowFuture = new CompletableFuture();

        // following steps will be executed in sequence to create an order for a customer

        // validate order received for logged in customer
        // validate create order request
        // update inventory of items in db
        // calculate other order details tax and total amount and create order details in mongo
        // process payment
        // create order response

        new ValidateLoggedInUserStep().execute(context)
                .thenCompose(new ValidateCreateOrderRequestStep()::execute)
                .thenCompose(new ConfirmAndUpdateItemsInventoryStep()::execute)
                .thenCompose(new CalculateOrderAmountsStep()::execute)
                .thenCompose(new CreateOrderStep()::execute)
                .thenAccept(context1 -> {

                    CreateOrderResponse createOrderResponse = new CreateOrderResponse();
                    createOrderResponse.setOrder(context.getOrder());
                    context.setResponsePayload(Json.encode(createOrderResponse));

                    logger.info("Workflow Completed");
                    ResponseStatus responseStatus = ResponseStatusMapper.getStatusCodeMap().get(ServiceAlerts.SUCCESS.getAlertCode());
                    context1.setResponseStatus(responseStatus);
                    workflowFuture.complete(context);
                }).exceptionally(throwable -> {
                    logger.error("Error occurred in executing workflow.");
                    workflowFuture.completeExceptionally(throwable);
                    return null;
                });

        return workflowFuture;
    }


}
