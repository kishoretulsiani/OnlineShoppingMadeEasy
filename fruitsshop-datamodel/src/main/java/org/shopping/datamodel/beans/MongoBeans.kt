package org.shopping.datamodel.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class ApplicationUser {
    var userId: String? = null;
    var userEmail: String? = null;
    var firstName: String? = null;
    var lastName: String? = null;
    var address: Address? = null;
    var docType: DocumentType = DocumentType.APPLICATION_USER;
}

enum class DocumentType {
    APPLICATION_USER, ORDER, ORDER_ITEM
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class Order {
    var orderDetails: OrderDetails? = null;
    var shippingAddress: Address? = null;
    var paymentDetails: PaymentDetails? = null;
    var orderAmountSummary: OrderAmountSummary? = null;
    var orderItems: List<OrderItem>? = null;
    var shippingSummary: List<ShippingDetails>? = null;
    var docType: DocumentType = DocumentType.ORDER;
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class OrderDetails {
    var orderId: String? = null;
    var orderDate: String? = null;
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class Address {
    var street: String? = null;
    var city: String? = null;
    var state: String? = null;
    var zipCode: String? = null;
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class PaymentDetails {
    var paymentId: String? = null;
    var paymentMethod: String? = null;
    var lastDigits: String? = null;
    var cvv: String? = null;
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class OrderAmountSummary {
    var orderSubTotalAmount: String? = null;
    var shippingAmount: String? = null;
    var totalTax: String? = null;
    var grandTotal: String? = null;
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class OrderItem {
    var itemId: String? = null;
    var itemName: String? = null;
    var itemDescription: String? = null;
    var itemQuantity: String? = null;
    var itemPrice: Integer? = null;
    var docType: DocumentType = DocumentType.ORDER_ITEM;
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class ShippingDetails {
    var itemId: String? = null;
    var shippingDate: String? = null;
}

