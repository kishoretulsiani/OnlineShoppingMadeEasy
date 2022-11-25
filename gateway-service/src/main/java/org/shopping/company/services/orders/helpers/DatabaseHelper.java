package org.shopping.company.services.orders.helpers;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.BulkOperation;
import org.shopping.common.components.constants.ServiceAlerts;
import org.shopping.common.components.exception.ApplicationException;
import org.shopping.common.components.mongo.MongoDB;
import org.shopping.common.components.utils.JsonUtility;
import org.shopping.company.services.orders.steps.CalculateOrderAmountsStep;
import org.shopping.company.services.orders.workflow.Context;
import org.shopping.datamodel.beans.DBCollections;
import org.shopping.datamodel.beans.Order;
import org.shopping.datamodel.beans.OrderItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class DatabaseHelper {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseHelper.class);


    public CompletableFuture<Boolean> orderCreateUpsert(Order order, Context context) {

        CompletableFuture<Boolean> orderUpdateFuture = new CompletableFuture();

        String orderObjStr = JsonUtility.getInstance().getString(order);
        JsonObject jsonOrderObject = new JsonObject(orderObjStr);

        JsonObject orderFilter = new JsonObject().put("orderId", order.getOrderId());
        BulkOperation replaceOrderDoc = BulkOperation.createReplace(orderFilter, jsonOrderObject).setUpsert(true);

        List<BulkOperation> itemOperations = Arrays.asList(replaceOrderDoc);

        MongoDB.getClient().bulkWrite(DBCollections.ORDERS.name(), itemOperations, itemResults -> {
            if (itemResults.succeeded()) {
                logger.info("Order updated successfully");
                orderUpdateFuture.complete(true);
            } else {
                logger.info("Order updated failed");
                orderUpdateFuture.complete(false);
            }
        });


        return orderUpdateFuture;

    }

    public CompletableFuture<Boolean> validateLoggedInUser(String requestUserId) {

        CompletableFuture<Boolean> validateLoggedInUserFuture = new CompletableFuture();

        JsonObject filter = new JsonObject().put("userId", requestUserId);

        MongoDB.getClient().find(DBCollections.APPLICATION_USERS.name(), filter, result -> {
            if (result.succeeded() && result.result().size() > 0) {
                logger.info("Userid in request successfully validated");
                validateLoggedInUserFuture.complete(true);
            } else {
                logger.info("Userid in request is invalid");
                validateLoggedInUserFuture.complete(false);
            }
        });


        return validateLoggedInUserFuture;

    }

    public CompletableFuture<List<OrderItem>> getOrderItems(List<OrderItem> requestOrderItems) {

        CompletableFuture<List<OrderItem>> getOrderItemsFuture = new CompletableFuture();

        ArrayList<String> itemIds = new ArrayList();
        requestOrderItems.forEach(orderItem -> {
            itemIds.add(orderItem.getItemId());
        });

        JsonObject queryParam = new JsonObject()
                .put("itemId", new JsonObject().put("$in", itemIds));

        MongoDB.getClient().find(DBCollections.ORDER_ITEMS.name(), queryParam, result -> {
            if (result.succeeded()) {
                List<JsonObject> itemArrayList = result.result();

                List<OrderItem> orderItemList = itemArrayList.stream().map(CalculateOrderAmountsStep::objectMapper).collect(Collectors.toList());

                getOrderItemsFuture.complete(orderItemList);

            } else {
                getOrderItemsFuture.completeExceptionally(new ApplicationException(ServiceAlerts.INTERNAL_ERROR.getAlertCode(), ServiceAlerts.INTERNAL_ERROR.getAlertMessage(), null));
            }
        });

        return getOrderItemsFuture;

    }


}
