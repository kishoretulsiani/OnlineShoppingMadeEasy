package org.shopping.company.services.orders.helpers;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.BulkOperation;
import org.shopping.common.components.constants.ServiceAlerts;
import org.shopping.common.components.exception.ApplicationException;
import org.shopping.common.components.mongo.MongoDB;
import org.shopping.common.components.utils.JsonUtility;
import org.shopping.company.services.orders.workflow.Context;
import org.shopping.datamodel.beans.ApplicationUser;
import org.shopping.datamodel.beans.DBCollections;
import org.shopping.datamodel.beans.DocumentType;
import org.shopping.datamodel.beans.Order;
import org.shopping.datamodel.beans.OrderItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class DatabaseHelper {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseHelper.class);

    private static HashMap<String, String> offersCache = null;


    public CompletableFuture<Boolean> orderCreateUpsert(Order order, Context context) {
        logger.info("CreateUpsert order");

        CompletableFuture<Boolean> orderUpdateFuture = new CompletableFuture();

        String orderObjStr = JsonUtility.getInstance().getString(order);
        JsonObject jsonOrderObject = new JsonObject(orderObjStr);

        JsonObject orderFilter = new JsonObject().put("orderId", order.getOrderId());
        BulkOperation replaceOrderDoc = BulkOperation.createReplace(orderFilter, jsonOrderObject).setUpsert(true);

        List<BulkOperation> itemOperations = Arrays.asList(replaceOrderDoc);

        MongoDB.getClient().bulkWrite(DBCollections.ORDERS.name(), itemOperations, itemResults -> {
            if (itemResults.succeeded()) {
                logger.info("Order CreateUpsert successful");
                orderUpdateFuture.complete(true);
            } else {
                logger.info("Order CreateUpsert failed");
                orderUpdateFuture.complete(false);
            }
        });

        return orderUpdateFuture;

    }

    public static Order orderMapper(JsonObject jsonObject) {

        String str = jsonObject.toString();
        Order order = JsonUtility.getInstance().getObject(str, Order.class);

        return order;
    }

    public static ApplicationUser userMapper(JsonObject jsonObject) {

        String str = jsonObject.toString();
        ApplicationUser applicationUser = JsonUtility.getInstance().getObject(str, ApplicationUser.class);

        return applicationUser;
    }

    public CompletableFuture<Boolean> updateApplicationUser(ApplicationUser user, String userId) {

        logger.info("updating Application user");

        CompletableFuture<Boolean> updateUserFuture = new CompletableFuture();

        String orderObjStr = JsonUtility.getInstance().getString(user);
        JsonObject jsonOrderObject = new JsonObject(orderObjStr);

        JsonObject userFilter = new JsonObject().put("userId", userId);
        BulkOperation replaceUserDoc = BulkOperation.createReplace(userFilter, jsonOrderObject).setUpsert(true);

        List<BulkOperation> itemOperations = Arrays.asList(replaceUserDoc);

        MongoDB.getClient().bulkWrite(DBCollections.APPLICATION_USERS.name(), itemOperations, itemResults -> {
            if (itemResults.succeeded()) {
                logger.info("updateApplicationUser updated successfully");
                updateUserFuture.complete(true);
            } else {
                logger.info("updateApplicationUser updated failed");
                updateUserFuture.completeExceptionally(new ApplicationException(ServiceAlerts.INTERNAL_ERROR.getAlertCode(), ServiceAlerts.INTERNAL_ERROR.getAlertMessage(), null));
            }
        });

        return updateUserFuture;

    }

    public CompletableFuture<ApplicationUser> getApplicationUser(String requestUserId) {

        CompletableFuture<ApplicationUser> validateLoggedInUserFuture = new CompletableFuture();

        JsonObject filter = new JsonObject().put("userId", requestUserId);

        MongoDB.getClient().find(DBCollections.APPLICATION_USERS.name(), filter, result -> {
            if (result.succeeded() && result.result().size() > 0) {
                logger.info("Userid in request successfully validated");
                List<JsonObject> itemArrayList = result.result();
                JsonObject userObj = itemArrayList.get(0);
                ApplicationUser user = DatabaseHelper.userMapper(userObj);
                validateLoggedInUserFuture.complete(user);
            } else {
                validateLoggedInUserFuture.completeExceptionally(new ApplicationException(ServiceAlerts.INVALID_USER.getAlertCode(), ServiceAlerts.INVALID_USER.getAlertMessage(), null));
            }
        });


        return validateLoggedInUserFuture;

    }

    public static OrderItem objectMapper(JsonObject jsonObject) {

        OrderItem orderItem = new OrderItem();
        orderItem.setItemId(jsonObject.getString("itemId"));
        orderItem.setItemQuantity(jsonObject.getString("itemQuantity"));
        orderItem.setItemName(jsonObject.getString("itemName"));
        orderItem.setItemDescription(jsonObject.getString("itemDescription"));
        orderItem.setItemPrice(jsonObject.getDouble("itemPrice"));

        return orderItem;
    }

    public CompletableFuture<List<String>> getUserOrderIds(String requestUserId) {

        logger.info("getting orderIds for user");

        CompletableFuture<List<String>> getUserOrdersFuture = new CompletableFuture();
        List<String> orderLdList = new ArrayList<>();

        JsonObject filter = new JsonObject().put("userId", requestUserId);

        MongoDB.getClient().find(DBCollections.APPLICATION_USERS.name(), filter, result -> {
            if (result.succeeded() && result.result().size() > 0) {
                logger.info("got orderIds for user");
                List<JsonObject> itemArrayList = result.result();

                JsonObject userObj = itemArrayList.get(0);

                for (int i = 0; i < userObj.getJsonArray("ordersIds").size(); i++) {
                    orderLdList.add(userObj.getJsonArray("ordersIds").getString(i));
                }
                getUserOrdersFuture.complete(orderLdList);
            } else {
                logger.info("user id does not have orders");
                getUserOrdersFuture.completeExceptionally(new ApplicationException(ServiceAlerts.NO_ORDERS_FOUND.getAlertCode(), ServiceAlerts.NO_ORDERS_FOUND.getAlertMessage(), null));
            }
        });


        return getUserOrdersFuture;

    }

    public CompletableFuture<List<Order>> getUserOrders(List<String> orderIds) {
        logger.info("getting order details for orderIds");
        CompletableFuture<List<Order>> getOrdersFuture = new CompletableFuture();

        JsonObject queryParam = new JsonObject().put("orderId", new JsonObject().put("$in", orderIds));

        MongoDB.getClient().find(DBCollections.ORDERS.name(), queryParam, result -> {
            if (result.succeeded()) {
                logger.info("fetched all order details for orderIds");
                List<JsonObject> ordersList = result.result();

                List<Order> orders = ordersList.stream().map(DatabaseHelper::orderMapper).collect(Collectors.toList());

                getOrdersFuture.complete(orders);

            } else {
                logger.error("could not fetch all order details for orderIds");
                getOrdersFuture.completeExceptionally(new ApplicationException(ServiceAlerts.INTERNAL_ERROR.getAlertCode(), ServiceAlerts.INTERNAL_ERROR.getAlertMessage(), null));
            }
        });

        return getOrdersFuture;

    }

    public CompletableFuture<List<OrderItem>> getOrderItems(List<OrderItem> requestOrderItems) {

        logger.info("getting order item details for requestOrderItems");
        CompletableFuture<List<OrderItem>> getOrderItemsFuture = new CompletableFuture();

        ArrayList<String> itemIds = new ArrayList();
        requestOrderItems.forEach(orderItem -> {
            itemIds.add(orderItem.getItemId());
        });

        JsonObject queryParam = new JsonObject().put("itemId", new JsonObject().put("$in", itemIds));

        MongoDB.getClient().find(DBCollections.ORDER_ITEMS.name(), queryParam, result -> {
            if (result.succeeded()) {
                logger.info("fetched all requestOrderItems details");
                List<JsonObject> itemArrayList = result.result();

                List<OrderItem> orderItemList = itemArrayList.stream().map(DatabaseHelper::objectMapper).collect(Collectors.toList());

                getOrderItemsFuture.complete(orderItemList);

            } else {
                logger.info("could not fetch all requestOrderItems details");
                getOrderItemsFuture.completeExceptionally(new ApplicationException(ServiceAlerts.INTERNAL_ERROR.getAlertCode(), ServiceAlerts.INTERNAL_ERROR.getAlertMessage(), null));
            }
        });

        return getOrderItemsFuture;

    }

    public CompletableFuture<HashMap<String, String>> getOffersCache() {
        logger.info("getting all offers from DB");

        CompletableFuture<HashMap<String, String>> getOffersFuture = new CompletableFuture();

        if (offersCache != null) {
            logger.info("returning offers from cache");
            getOffersFuture.complete(offersCache);
        } else {
            logger.info("returning offers from DB");
            offersCache = new HashMap<>();
            JsonObject queryParam = new JsonObject().put("docType", DocumentType.OFFER.name());

            MongoDB.getClient().find(DBCollections.OFFERS.name(), queryParam, result -> {
                if (result.succeeded()) {
                    logger.info("retrieved all offer details and building cache now");
                    List<JsonObject> offersList = result.result();
                    offersList.forEach(offer -> {
                        String offerType = offer.getString("offerType");
                        List<String> applicableItemsList = offer.getJsonArray("applicableItems").getList();
                        for (String itemId : applicableItemsList) {
                            offersCache.put(itemId, offerType);
                        }
                    });
                    getOffersFuture.complete(offersCache);
                } else {
                    logger.info("error occurred while getting offer details");
                    getOffersFuture.completeExceptionally(new ApplicationException(ServiceAlerts.INTERNAL_ERROR.getAlertCode(), ServiceAlerts.INTERNAL_ERROR.getAlertMessage(), null));
                }
            });
        }

        return getOffersFuture;

    }

}
