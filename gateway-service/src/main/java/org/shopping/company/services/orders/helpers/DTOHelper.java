package org.shopping.company.services.orders.helpers;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.shopping.company.services.orders.workflow.Context;
import org.shopping.datamodel.beans.ORDER_STATUS;
import org.shopping.datamodel.beans.Order;
import org.shopping.datamodel.beans.OrderDetails;
import org.shopping.datamodel.beans.OrderItem;
import org.shopping.datamodel.beans.PaymentDetails;
import org.shopping.datamodel.beans.ShippingDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DTOHelper {

    private static final Logger logger = LoggerFactory.getLogger(DTOHelper.class);


    public void createOrderDetails(Order order) {
        OrderDetails orderDetails = new OrderDetails();
        order.setOrderId(UUID.randomUUID().toString());
        orderDetails.setOrderDate(LocalDateTime.now().toString());
        orderDetails.setOrderStatus(ORDER_STATUS.PROCESSING);
        order.setOrderDetails(orderDetails);
    }

    public void createPaymentDetails(Order order) {
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setPaymentId(UUID.randomUUID().toString());
        paymentDetails.setLastDigits("2244");
        paymentDetails.setPaymentMethod("CARD");
        order.setPaymentDetails(paymentDetails);
    }

    public void createShippingDetails(Order order, Context context) {
        ArrayList<ShippingDetails> shippingDetailsArrayList = new ArrayList<>();
        for (OrderItem orderItem : context.getOrder().getOrderItems()) {
            ShippingDetails shippingDetailsItems = new ShippingDetails();
            shippingDetailsItems.setItemId(orderItem.getItemId());
            shippingDetailsItems.setShippingDate(LocalDateTime.now().toString());
            shippingDetailsArrayList.add(shippingDetailsItems);
        }
        order.setShippingSummary(shippingDetailsArrayList);
    }

    public void updateOrderQuantity(List<OrderItem> orderItemsInRequest, List<OrderItem> responseOrderItems) {

        HashMap<String, Integer> itemIdAndQuantity = new HashMap<>();
        orderItemsInRequest.forEach(orderItem -> {
            itemIdAndQuantity.put(orderItem.getItemId(), Integer.valueOf(orderItem.getItemQuantity()));
        });

        responseOrderItems.forEach(orderItem -> orderItem.setItemQuantity(String.valueOf(itemIdAndQuantity.get(orderItem.getItemId()))));

    }


}
