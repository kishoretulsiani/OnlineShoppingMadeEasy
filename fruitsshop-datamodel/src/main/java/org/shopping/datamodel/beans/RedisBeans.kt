package org.shopping.datamodel.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class Inventory {
    var itemId: String? = null;
    var itemQuantity: String? = null;
}
