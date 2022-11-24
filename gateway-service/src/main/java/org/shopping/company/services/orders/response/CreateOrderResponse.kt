package org.shopping.company.services.orders.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import org.shopping.datamodel.beans.*
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class CreateOrderResponse {
    var order: Order? = null;
}
