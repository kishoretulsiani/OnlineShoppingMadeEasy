package org.shopping.company.services.orders.steps;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.shopping.company.services.orders.helpers.DTOHelper;
import org.shopping.company.services.orders.helpers.DatabaseHelper;
import org.shopping.company.services.orders.helpers.RedisHelper;
import org.shopping.company.services.orders.request.CreateOrderRequest;
import org.shopping.company.services.orders.workflow.Context;
import org.shopping.company.services.orders.workflow.WorkflowStep;
import org.shopping.datamodel.beans.Order;
import org.shopping.datamodel.beans.OrderItem;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class ConfirmAndUpdateItemsInventoryStep implements WorkflowStep {

    private final Logger logger = LoggerFactory.getLogger(ConfirmAndUpdateItemsInventoryStep.class);

    DatabaseHelper databaseHelper;

    DTOHelper dtoHelper;

    RedisHelper redisHelper;

    public ConfirmAndUpdateItemsInventoryStep(DatabaseHelper databaseHelper, DTOHelper dtoHelper, RedisHelper redisHelper) {
        this.databaseHelper = databaseHelper;
        this.dtoHelper = dtoHelper;
        this.redisHelper = redisHelper;
    }

    @Override
    public CompletableFuture<Context> execute(Context context) {
        logger.info("Workflow ConfirmAndUpdateItemsInventoryStep Executed");

        CompletableFuture<Context> confirmAndUpdateItemsInventoryStepFuture = new CompletableFuture();

        CreateOrderRequest createOrderRequest = context.getCreateOrderRequest();

        ArrayList<OrderItem> orderItemArrayList = createOrderRequest.getItemDetails();

        ArrayList<CompletableFuture<Void>> completableFutureArrayList = new ArrayList<>();

        ArrayList<CompletableFuture<Boolean>> updateInventoryFutureList = new ArrayList<>();

        orderItemArrayList.forEach(orderItem -> {
            redisHelper.getItemInventoryIfExists(orderItem.getItemId(), orderItem.getItemQuantity())
                    .thenCompose(itemInventory -> {
                        updateInventoryFutureList.add(redisHelper.updateInventory(orderItem.getItemId(), (itemInventory - Integer.valueOf(orderItem.getItemQuantity()))));
                        return null;
                    });

        });

        CompletableFuture<Void>[] updateInventoryFutureArray = new CompletableFuture[updateInventoryFutureList.size()];
        updateInventoryFutureList.toArray(updateInventoryFutureArray);
        CompletableFuture.allOf(updateInventoryFutureArray).thenAccept(unused -> {
            Order order = new Order();
            dtoHelper.createOrderDetails(order);
            context.setOrder(order);
            confirmAndUpdateItemsInventoryStepFuture.complete(context);
        });


        return confirmAndUpdateItemsInventoryStepFuture;
    }
}
