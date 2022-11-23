package org.shopping.company.gateway.singleton

import org.shopping.company.services.orders.OrdersService

object GatewaySingleton {
    var ordersService: OrdersService? = null

}
