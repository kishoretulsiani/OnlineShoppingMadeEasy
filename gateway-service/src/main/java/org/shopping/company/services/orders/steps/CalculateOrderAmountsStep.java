package org.shopping.company.services.orders.steps;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.shopping.company.services.orders.helpers.DTOHelper;
import org.shopping.company.services.orders.helpers.DatabaseHelper;
import org.shopping.company.services.orders.helpers.RedisHelper;
import org.shopping.company.services.orders.request.CreateOrderRequest;
import org.shopping.company.services.orders.workflow.Context;
import org.shopping.company.services.orders.workflow.WorkflowStep;
import org.shopping.datamodel.beans.OrderAmountSummary;
import org.shopping.datamodel.beans.OrderItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CalculateOrderAmountsStep implements WorkflowStep {

    private final Logger logger = LoggerFactory.getLogger(CalculateOrderAmountsStep.class);

    DatabaseHelper databaseHelper;

    DTOHelper dtoHelper;

    RedisHelper redisHelper;


    public CalculateOrderAmountsStep(DatabaseHelper databaseHelper, DTOHelper dtoHelper, RedisHelper redisHelper) {
        this.databaseHelper = databaseHelper;
        this.dtoHelper = dtoHelper;
        this.redisHelper = redisHelper;
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

        context.getOrder().setOrderAmountSummary(orderAmountSummary);


    }

    @Override
    public CompletableFuture<Context> execute(Context context) {
        logger.info("Workflow CalculateOrderAmountsStep Executed");

        CompletableFuture<Context> calculateOrderAmountsStepFuture = new CompletableFuture();

        CreateOrderRequest createOrderRequest = context.getCreateOrderRequest();

        ArrayList<OrderItem> requestOrderItemArrayList = createOrderRequest.getItemDetails();

        databaseHelper.getOrderItems(requestOrderItemArrayList).thenAccept(orderItems -> {

            createOrderAmountsSummary(orderItems, requestOrderItemArrayList, context);

            dtoHelper.updateOrderQuantity(requestOrderItemArrayList, orderItems);

            context.getOrder().setOrderItems(orderItems);

            databaseHelper.orderCreateUpsert(context.getOrder(), context).thenAccept(aBoolean -> calculateOrderAmountsStepFuture.complete(context));

        });

        return calculateOrderAmountsStepFuture;
    }
}
