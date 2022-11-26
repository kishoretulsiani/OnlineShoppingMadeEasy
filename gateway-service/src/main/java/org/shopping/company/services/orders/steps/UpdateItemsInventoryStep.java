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
import org.shopping.datamodel.beans.Order;
import org.shopping.datamodel.beans.OrderItem;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class UpdateItemsInventoryStep implements WorkflowStep {

    private final Logger logger = LoggerFactory.getLogger(UpdateItemsInventoryStep.class);

    DatabaseHelper databaseHelper;

    DTOHelper dtoHelper;

    RedisHelper redisHelper;

    public UpdateItemsInventoryStep(DatabaseHelper databaseHelper, DTOHelper dtoHelper, RedisHelper redisHelper) {
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

        ArrayList<CompletableFuture<Boolean>> itemInventoryFutureList = new ArrayList<>();


        orderItemArrayList.forEach(orderItem -> {
            itemInventoryFutureList.add(
                    redisHelper.updateInventory(orderItem.getItemId(), (context.getItemInventory().get(orderItem.getItemId()) - Integer.valueOf(orderItem.getItemQuantity()))
                    ));
        });


        CompletableFuture<Void> allFutures = CompletableFuture.allOf(itemInventoryFutureList.toArray(new CompletableFuture[itemInventoryFutureList.size()]));

        allFutures.thenApply(unused -> {

            Order order = new Order();
            dtoHelper.createOrderDetails(order);
            context.setOrder(order);

            confirmAndUpdateItemsInventoryStepFuture.complete(context);

            return null;
        }).exceptionally(throwable -> {
            confirmAndUpdateItemsInventoryStepFuture.completeExceptionally(new ApplicationException(ServiceAlerts.INTERNAL_ERROR.getAlertCode(), ServiceAlerts.INTERNAL_ERROR.getAlertMessage(), null));

            return null;
        });


        return confirmAndUpdateItemsInventoryStepFuture;
    }
}
