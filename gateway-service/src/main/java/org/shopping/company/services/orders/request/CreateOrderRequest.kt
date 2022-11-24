package org.shopping.company.services.orders.request;

import org.shopping.datamodel.beans.OrderItem
import org.shopping.datamodel.beans.PaymentDetails


class CreateOrderRequest {
    var userId: String? = null;
    var itemDetails: ArrayList<OrderItem>? = null;
    var paymentDetails: PaymentDetails? = null;
    var shippingAddressId: String? = null;
}
