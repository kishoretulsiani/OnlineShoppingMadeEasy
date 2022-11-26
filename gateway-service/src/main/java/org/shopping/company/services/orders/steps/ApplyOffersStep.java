package org.shopping.company.services.orders.steps;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.shopping.company.services.orders.helpers.DTOHelper;
import org.shopping.company.services.orders.helpers.DatabaseHelper;
import org.shopping.company.services.orders.helpers.RedisHelper;
import org.shopping.company.services.orders.workflow.Context;
import org.shopping.company.services.orders.workflow.WorkflowStep;
import org.shopping.datamodel.beans.OrderAmountSummary;
import org.shopping.datamodel.beans.OrderItem;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ApplyOffersStep implements WorkflowStep {

    private final Logger logger = LoggerFactory.getLogger(ApplyOffersStep.class);

    DatabaseHelper databaseHelper;

    DTOHelper dtoHelper;

    RedisHelper redisHelper;


    public ApplyOffersStep(DatabaseHelper databaseHelper, DTOHelper dtoHelper, RedisHelper redisHelper) {
        this.databaseHelper = databaseHelper;
        this.dtoHelper = dtoHelper;
        this.redisHelper = redisHelper;
    }


    @Override
    public CompletableFuture<Context> execute(Context context) {
        logger.info("Workflow ApplyOffersStep Executed");

        CompletableFuture<Context> applyOffersStep = new CompletableFuture();

        List<OrderItem> orderItems = context.getOrder().getOrderItems();

        OrderAmountSummary orderAmountSummary = context.getOrder().getOrderAmountSummary();

        databaseHelper.getOffersCache().thenAccept(offersCache -> {

            double discountAmount = calculateAndApplyDiscount(orderItems, offersCache);

            if (discountAmount > 0) {
                recalculateOrderSummary(orderAmountSummary, discountAmount);
            }

            applyOffersStep.complete(context);

        });

        return applyOffersStep;
    }

    private void recalculateOrderSummary(OrderAmountSummary orderAmountSummary, double discountAmount) {

        orderAmountSummary.setTotalDiscount(String.valueOf(discountAmount));
        double orderSubTotalAmount = Double.valueOf(orderAmountSummary.getOrderSubTotalAmount());
        double shippingAmount = Double.valueOf(orderAmountSummary.getShippingAmount());
        double totalTax = 0.0;
        orderSubTotalAmount = orderSubTotalAmount - discountAmount;
        totalTax = 0.15 * orderSubTotalAmount;
        totalTax = Math.round(totalTax * 100.0) / 100.0;

        double grandTotal = orderSubTotalAmount + totalTax + shippingAmount;
        // rounding to two decimal places
        grandTotal = Math.round(grandTotal * 100.0) / 100.0;
        orderAmountSummary.setOrderSubTotalAmount(String.valueOf(orderSubTotalAmount));
        orderAmountSummary.setShippingAmount(String.valueOf(shippingAmount));
        orderAmountSummary.setTotalTax(String.valueOf(totalTax));
        orderAmountSummary.setGrandTotal(String.valueOf(grandTotal));
    }


    private double calculateAndApplyDiscount(List<OrderItem> orderItems, HashMap<String, String> offersCache) {

        double discountAmount = 0.0;

        for (OrderItem item : orderItems) {
            double itemDiscountAmount = 0.0;
            if (offersCache.get(item.getItemId()) != null) {
                String offerType = offersCache.get(item.getItemId());
                itemDiscountAmount = offerDiscount(offerType, item.getItemPrice(), Double.valueOf(item.getItemQuantity()));
                discountAmount = discountAmount + itemDiscountAmount;
                item.setItemDiscount(itemDiscountAmount);
            } else {
                // there is no offer for this item
            }
        }
        return discountAmount;
    }

    // TODO improve the method by functional programming and creating separate method by each offer type.
    private double offerDiscount(String offerType, Double itemPrice, Double itemQuantity) {

        double itemDiscount = 0.0;

        switch (offerType) {
            case "BUY_ONE_GET_ONE_FREE":
                // current implementation applies the 50% discount on item price for even number of items.
                // various approaches can be discussed adn implemented for this offer.
                if ((itemQuantity % 2) == 0) {
                    // even number of items
                    itemDiscount = (itemQuantity * itemPrice) / 2;
                } else {
                    // odd number of items
                    itemDiscount = ((itemQuantity - 1) * itemPrice) / 2;
                }
                break;
            case "3_FOR_THE_PRICE_OF_2_ON_ORANGES":
                // as per current implementation the discount for this offer is calculated as 33% of item price.
                // only if item quantity is more than 3
                // various approaches can be discussed adn implemented for this offer.
                if (itemQuantity >= 3) {
                    itemDiscount = (itemQuantity * itemPrice) * 0.33;
                }
                break;
            default:
                return 0.0;
        }

        // rounding discount to two decimal places
        itemDiscount = Math.round(itemDiscount * 100.0) / 100.0;

        return itemDiscount;
    }
}
