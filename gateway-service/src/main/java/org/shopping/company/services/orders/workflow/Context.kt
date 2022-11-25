package org.shopping.company.services.orders.workflow;

import io.vertx.core.json.JsonObject
import org.shopping.common.components.constants.ResponseStatus
import org.shopping.company.services.orders.request.CreateOrderRequest
import org.shopping.company.services.orders.response.CreateOrderResponse
import org.shopping.datamodel.beans.Order


data class Context(
        var requestObject: JsonObject? = null,
        var responsePayload: String? = null,
        var responseStatus: ResponseStatus? = null,
        var trackingId: String? = null,
        var createOrderRequest: CreateOrderRequest? = null,
        var createOrderResponse: CreateOrderResponse? = null,
        var order: Order? = null


);

