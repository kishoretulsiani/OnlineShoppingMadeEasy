package org.shopping.company.services.orders.steps;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.shopping.common.components.constants.ServiceAlerts;
import org.shopping.common.components.exception.ApplicationException;
import org.shopping.common.components.redis.RedisCache;
import org.shopping.company.services.orders.request.CreateOrderRequest;
import org.shopping.company.services.orders.workflow.Context;
import org.shopping.datamodel.beans.OrderItem;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class ConfirmAndUpdateItemsInventoryStep implements WorkflowStep {

    private final Logger logger = LoggerFactory.getLogger(ConfirmAndUpdateItemsInventoryStep.class);


    @Override
    public CompletableFuture<Context> execute(Context context) {
        logger.info("Workflow ConfirmAndUpdateItemsInventoryStep Executed");

        CompletableFuture<Context> confirmAndUpdateItemsInventoryStepFuture = new CompletableFuture();

        CreateOrderRequest createOrderRequest = context.getCreateOrderRequest();

        ArrayList<OrderItem> orderItemArrayList = createOrderRequest.getItemDetails();

        ArrayList<CompletableFuture<Void>> completableFutureArrayList = new ArrayList<>();

        for (OrderItem orderItem : orderItemArrayList) {

            CompletableFuture<Void> itemInventoryUpdateCompletableFuture = new CompletableFuture();
            completableFutureArrayList.add(itemInventoryUpdateCompletableFuture);

            RedisCache.getClient().get(orderItem.getItemId(), inventoryResult -> {

                if (inventoryResult.succeeded()) {

                    Integer itemInventory = Integer.valueOf(inventoryResult.result());
                    Integer itemQuantity = Integer.valueOf(orderItem.getItemQuantity());

                    if (itemInventory == 0 || itemInventory < itemQuantity) {
                        confirmAndUpdateItemsInventoryStepFuture.completeExceptionally(new ApplicationException(ServiceAlerts.ITEM_OUT_OF_STOCK.getAlertCode(), ServiceAlerts.ITEM_OUT_OF_STOCK.getAlertMessage(), null));
                    } else {
                        RedisCache.getClient().set(orderItem.getItemId(), String.valueOf(itemInventory - itemQuantity), voidAsyncResult -> {
                            if (voidAsyncResult.succeeded()) {
                                logger.info("item inventory updated in redis" + orderItem.getItemId());
                                itemInventoryUpdateCompletableFuture.complete(null);
                            } else {
                                itemInventoryUpdateCompletableFuture.completeExceptionally(new ApplicationException(ServiceAlerts.INTERNAL_ERROR.getAlertCode(), ServiceAlerts.INTERNAL_ERROR.getAlertMessage(), null));
                            }
                        });
                    }
                } else {
                    itemInventoryUpdateCompletableFuture.completeExceptionally(new ApplicationException(ServiceAlerts.INTERNAL_ERROR.getAlertCode(), ServiceAlerts.INTERNAL_ERROR.getAlertMessage(), null));
                }

            });

        }

        CompletableFuture<Void>[] completableFutures = new CompletableFuture[completableFutureArrayList.size()];
        completableFutureArrayList.toArray(completableFutures);
        CompletableFuture.allOf(completableFutures).thenAccept(unused -> confirmAndUpdateItemsInventoryStepFuture.complete(context));

        return confirmAndUpdateItemsInventoryStepFuture;
    }
}
