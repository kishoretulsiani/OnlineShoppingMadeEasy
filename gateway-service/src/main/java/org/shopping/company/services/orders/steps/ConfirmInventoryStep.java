package org.shopping.company.services.orders.steps;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.shopping.common.components.constants.ServiceAlerts;
import org.shopping.common.components.exception.ApplicationException;
import org.shopping.company.services.orders.helpers.DTOHelper;
import org.shopping.company.services.orders.helpers.DatabaseHelper;
import org.shopping.company.services.orders.helpers.RedisHelper;
import org.shopping.company.services.orders.request.CreateOrderRequest;
import org.shopping.company.services.orders.workflow.Context;
import org.shopping.company.services.orders.workflow.WorkflowStep;
import org.shopping.datamodel.beans.OrderItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class ConfirmInventoryStep implements WorkflowStep {

    private final Logger logger = LoggerFactory.getLogger(ConfirmInventoryStep.class);

    DatabaseHelper databaseHelper;

    DTOHelper dtoHelper;

    RedisHelper redisHelper;

    public ConfirmInventoryStep(DatabaseHelper databaseHelper, DTOHelper dtoHelper, RedisHelper redisHelper) {
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

        ArrayList<CompletableFuture<Void>> itemInventoryFutureList = new ArrayList<>();

        HashMap<String, Integer> itemInventory = new HashMap<>();

        orderItemArrayList.forEach(orderItem -> {
            itemInventoryFutureList.add(
                    redisHelper.getItemInventoryIfExists(orderItem.getItemId(), orderItem.getItemQuantity()).thenAccept(integer -> {
                        itemInventory.put(orderItem.getItemId(), integer);
                    })
            );
        });


        CompletableFuture<Void> allFutures = CompletableFuture.allOf(itemInventoryFutureList.toArray(new CompletableFuture[itemInventoryFutureList.size()]));

        allFutures.thenApply(unused -> {

            context.setItemInventory(itemInventory);
            confirmAndUpdateItemsInventoryStepFuture.complete(context);

            return null;
        }).exceptionally(throwable -> {
            confirmAndUpdateItemsInventoryStepFuture.completeExceptionally(new ApplicationException(ServiceAlerts.ITEM_OUT_OF_STOCK.getAlertCode(), ServiceAlerts.ITEM_OUT_OF_STOCK.getAlertMessage(), null));

            return null;
        });


        return confirmAndUpdateItemsInventoryStepFuture;
    }
}
