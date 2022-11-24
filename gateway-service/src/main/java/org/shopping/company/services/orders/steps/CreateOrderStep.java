package org.shopping.company.services.orders.steps;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.shopping.common.components.constants.ServiceAlerts;
import org.shopping.common.components.exception.ApplicationException;
import org.shopping.common.components.mongo.MongoDB;
import org.shopping.common.components.utils.JsonUtility;
import org.shopping.company.services.orders.workflow.Context;
import org.shopping.datamodel.beans.Order;
import org.shopping.datamodel.beans.OrderAmountSummary;
import org.shopping.datamodel.beans.OrderDetails;
import org.shopping.datamodel.beans.OrderItem;
import org.shopping.datamodel.beans.PaymentDetails;
import org.shopping.datamodel.beans.ShippingDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CreateOrderStep implements WorkflowStep {

    private final Logger logger = LoggerFactory.getLogger(CreateOrderStep.class);

    @Override
    public CompletableFuture<Context> execute(Context context) {
        logger.info("Workflow Step2 Executed");
        CompletableFuture<Context> createOrderStepFuture = new CompletableFuture();

        Order order = new Order();

        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setOrderId(UUID.randomUUID().toString());
        orderDetails.setOrderDate(LocalDateTime.now().toString());

        OrderAmountSummary orderAmountSummary = context.getOrderAmountSummary();

        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setPaymentId(UUID.randomUUID().toString());
        paymentDetails.setLastDigits("2244");
        paymentDetails.setPaymentMethod("CARD");


        List<OrderItem> orderItemArrayList = context.getOrderItemList();


        ArrayList<ShippingDetails> shippingDetailsArrayList = new ArrayList<>();
        for (OrderItem orderItem : context.getOrderItemList()) {
            ShippingDetails shippingDetailsItems = new ShippingDetails();
            shippingDetailsItems.setItemId(orderItem.getItemId());
            shippingDetailsItems.setShippingDate(LocalDateTime.now().toString());
            shippingDetailsArrayList.add(shippingDetailsItems);
        }

        order.setOrderDetails(orderDetails);
        order.setOrderAmountSummary(orderAmountSummary);
        order.setPaymentDetails(paymentDetails);
        order.setOrderItems(orderItemArrayList);
        order.setShippingSummary(shippingDetailsArrayList);

        context.setOrder(order);

        String orderObjStr = JsonUtility.getInstance().getString(order);
        JsonObject jsonOrderObject = new JsonObject(orderObjStr);

        MongoDB.getClient().insert("ORDERS", jsonOrderObject, result -> {
            if (result.succeeded()) {
                createOrderStepFuture.complete(context);
            } else {
                createOrderStepFuture.completeExceptionally(new ApplicationException(ServiceAlerts.INTERNAL_ERROR.getAlertCode(), ServiceAlerts.INTERNAL_ERROR.getAlertMessage(), null));

            }
        });


        return createOrderStepFuture;
    }
}
