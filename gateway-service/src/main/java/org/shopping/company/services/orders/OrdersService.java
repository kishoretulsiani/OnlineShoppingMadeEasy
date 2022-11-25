package org.shopping.company.services.orders;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.serviceproxy.ServiceProxyBuilder;

@VertxGen
@ProxyGen
public interface OrdersService {

    String ORDERS_SERVICE_ADDRESS = "service.orders";

    long DEFAULT_TIMEOUT = 3000L;

    static OrdersService createProxy(Vertx vertx) {
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setSendTimeout(DEFAULT_TIMEOUT);
        ServiceProxyBuilder serviceProxyBuilder = new ServiceProxyBuilder(vertx).setAddress(
                ORDERS_SERVICE_ADDRESS).setOptions(deliveryOptions);
        return serviceProxyBuilder.build(OrdersService.class);
    }

    static void register(Vertx vertx) {
        new ServiceBinder(vertx).setAddress(ORDERS_SERVICE_ADDRESS)
                .register(OrdersService.class, new OrdersServiceImpl(vertx));
    }

    void createOrder(JsonObject request, Handler<AsyncResult<JsonObject>> asyncResultHandler);

}
