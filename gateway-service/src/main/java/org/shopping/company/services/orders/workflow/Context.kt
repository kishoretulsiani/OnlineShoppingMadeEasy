package org.shopping.company.services.orders.workflow;

import io.vertx.core.json.JsonObject
import org.shopping.common.components.constants.ResponseStatus
import org.shopping.company.services.orders.request.CreateOrderRequest
import org.shopping.company.services.orders.request.RetrieveOrdersRequest
import org.shopping.company.services.orders.response.CreateOrderResponse
import org.shopping.company.services.orders.response.RetrieveOrdersResponse
import org.shopping.datamodel.beans.ApplicationUser
import org.shopping.datamodel.beans.Order


data class Context(
        var requestObject: JsonObject? = null,
        var responsePayload: String? = null,
        var responseStatus: ResponseStatus? = null,
        var trackingId: String? = null,
        var createOrderRequest: CreateOrderRequest? = null,
        var createOrderResponse: CreateOrderResponse? = null,

        var retrieveOrdersRequest: RetrieveOrdersRequest? = null,
        var retrieveOrdersResponse: RetrieveOrdersResponse? = null,
        var userOrderIds: List<String>? = null,
        var orders: List<Order>? = null,
        var loggedInUser: ApplicationUser? = null,

        var itemInventory: HashMap<String?, Int?>? = null,
        var isOrderSavedInDB: Boolean? = null,
        var order: Order? = null


);

