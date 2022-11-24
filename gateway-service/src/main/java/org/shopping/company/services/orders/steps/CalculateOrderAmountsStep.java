package org.shopping.company.services.orders.steps;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.shopping.common.components.constants.ServiceAlerts;
import org.shopping.common.components.exception.ApplicationException;
import org.shopping.common.components.mongo.MongoDB;
import org.shopping.company.services.orders.request.CreateOrderRequest;
import org.shopping.company.services.orders.workflow.Context;
import org.shopping.datamodel.beans.OrderAmountSummary;
import org.shopping.datamodel.beans.OrderItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CalculateOrderAmountsStep implements WorkflowStep {

    private final Logger logger = LoggerFactory.getLogger(CalculateOrderAmountsStep.class);

    public static OrderItem objectMapper(JsonObject jsonObject) {

        OrderItem orderItem = new OrderItem();
        orderItem.setItemId(jsonObject.getString("itemId"));
        orderItem.setItemQuantity(jsonObject.getString("itemQuantity"));
        orderItem.setItemName(jsonObject.getString("itemName"));
        orderItem.setItemDescription(jsonObject.getString("itemDescription"));
        orderItem.setItemPrice(jsonObject.getInteger("itemPrice"));

        return orderItem;
    }

    public static void createOrderAmountsSummary(List<OrderItem> orderItemList, ArrayList<OrderItem> requestOrderItemArrayList, Context context) {
        OrderAmountSummary orderAmountSummary = new OrderAmountSummary();

        HashMap<String, Integer> itemIdAndQuantity = new HashMap<>();
        requestOrderItemArrayList.forEach(orderItem -> {
            itemIdAndQuantity.put(orderItem.getItemId(), Integer.valueOf(orderItem.getItemQuantity()));
        });

        double orderSubTotalAmount = 0;
        double shippingAmount = 10;
        double totalTax = 0;
        double grandTotal = 0;

        for (OrderItem orderItem : orderItemList) {
            orderSubTotalAmount = orderSubTotalAmount + (orderItem.getItemPrice() * itemIdAndQuantity.get(orderItem.getItemId()));
        }

        totalTax = 0.15 * orderSubTotalAmount;


        grandTotal = orderSubTotalAmount + totalTax + shippingAmount;
        orderAmountSummary.setOrderSubTotalAmount(String.valueOf(orderSubTotalAmount));
        orderAmountSummary.setShippingAmount(String.valueOf(shippingAmount));
        orderAmountSummary.setTotalTax(String.valueOf(totalTax));
        orderAmountSummary.setGrandTotal(String.valueOf(grandTotal));

        context.setOrderAmountSummary(orderAmountSummary);


    }

    @Override
    public CompletableFuture<Context> execute(Context context) {
        logger.info("Workflow CalculateOrderAmountsStep Executed");

        CompletableFuture<Context> calculateOrderAmountsStepFuture = new CompletableFuture();

        CreateOrderRequest createOrderRequest = context.getCreateOrderRequest();

        ArrayList<OrderItem> requestOrderItemArrayList = createOrderRequest.getItemDetails();


        ArrayList<String> itemIds = new ArrayList();
        requestOrderItemArrayList.forEach(orderItem -> {
            itemIds.add(orderItem.getItemId());
        });

        HashMap<String, Integer> itemIdAndQuantity = new HashMap<>();
        requestOrderItemArrayList.forEach(orderItem -> {
            itemIdAndQuantity.put(orderItem.getItemId(), Integer.valueOf(orderItem.getItemQuantity()));
        });

        JsonObject queryParam = new JsonObject()
                .put("itemId", new JsonObject().put("$in", itemIds));

        MongoDB.getClient().find("ORDER_ITEMS", queryParam, result -> {
            if (result.succeeded()) {
                List<JsonObject> itemArrayList = result.result();

                List<OrderItem> orderItemList = itemArrayList.stream().map(CalculateOrderAmountsStep::objectMapper).collect(Collectors.toList());

                orderItemList.forEach(orderItem -> orderItem.setItemQuantity(String.valueOf(itemIdAndQuantity.get(orderItem.getItemId()))));

                context.setOrderItemList(orderItemList);

                createOrderAmountsSummary(orderItemList, requestOrderItemArrayList, context);

                logger.info("got all item details successfully... ");

                calculateOrderAmountsStepFuture.complete(context);
            } else {
                calculateOrderAmountsStepFuture.completeExceptionally(new ApplicationException(ServiceAlerts.INTERNAL_ERROR.getAlertCode(), ServiceAlerts.INTERNAL_ERROR.getAlertMessage(), null));
            }
        });

        return calculateOrderAmountsStepFuture;
    }
}
