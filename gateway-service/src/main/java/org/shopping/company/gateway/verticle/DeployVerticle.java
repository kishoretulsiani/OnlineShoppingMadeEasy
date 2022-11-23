package org.shopping.company.gateway.verticle;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.TimeoutHandler;
import org.shopping.common.components.constants.ResponseStatusMapper;
import org.shopping.common.components.mongo.MongoDB;
import org.shopping.common.components.redis.RedisCache;
import org.shopping.company.gateway.constants.GatewayConstant;
import org.shopping.company.gateway.handler.ExceptionHandler;
import org.shopping.company.gateway.handler.ResponseHandler;
import org.shopping.company.services.orders.OrdersService;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class DeployVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(DeployVerticle.class);

    private static final String API_CONTEXT_ROOT = "/shopping/company/*";

    private static final String BASE_PATH = "/shopping/company/";

    private static final String ORDERS_API = "orders";

    private static OrdersService ordersService = null;

    private static Set<String> setCorsHeaders() {
        Set<String> allowHeaders = new HashSet<>();
        allowHeaders.add(GatewayConstant.CONTENT_TYPE);
        allowHeaders.add(GatewayConstant.CONTENT_LENGTH);
        allowHeaders.add(GatewayConstant.ACCESS_CONTROL_ALLOW_ORIGIN);
        allowHeaders.add(GatewayConstant.ACCESS_CONTROL_ALLOW_CREDENTIALS);
        allowHeaders.add(GatewayConstant.ACCESS_CONTROL_ALLOW_HEADERS);
        allowHeaders.add(GatewayConstant.ACCESS_CONTROL_REQUEST_METHOD);
        allowHeaders.add(GatewayConstant.X_TRACKING_ID);
        allowHeaders.add("Authorization");
        allowHeaders.add("Accept-Language");
        allowHeaders.add("app_version");
        allowHeaders.add("device_type");
        allowHeaders.add("User-Authorization");
        allowHeaders.add("authorization_code");
        allowHeaders.add("requester_id");
        allowHeaders.add("account_token");
        allowHeaders.add("mock-response-key");
        return allowHeaders;

    }

    private static Set<String> setCorsExposedHeaders() {
        Set<String> exposeHeaders = new HashSet<>();
        exposeHeaders.add("Custom-Header");
        exposeHeaders.add("x-amex-tracking-id");
        return exposeHeaders;
    }

    private static Set<HttpMethod> setCorsMethods() {
        Set<HttpMethod> allowMethods = new HashSet<>();
        allowMethods.add(HttpMethod.GET);
        allowMethods.add(HttpMethod.POST);
        allowMethods.add(HttpMethod.OPTIONS);
        allowMethods.add(HttpMethod.PUT);
        return allowMethods;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        Map<String, String> env = System.getenv();

        // initializing MongoDB
        Future mongoFuture = MongoDB.initialize(vertx);

        // initializing Redis
        Future redisFuture = RedisCache.initialize(vertx);

        // initializing OrderService
        OrdersService.register(vertx);
        ordersService = OrdersService.createProxy(vertx);

        ResponseStatusMapper responseStatusMapper = new ResponseStatusMapper();

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route().handler(TimeoutHandler.create(8000, 501));
        router.route(API_CONTEXT_ROOT).failureHandler(new ExceptionHandler(responseStatusMapper));
        router.route().handler(this::getCORSHandler);
        router.post(BASE_PATH + ORDERS_API).handler(this::handleOrdersRequest);
        router.get("/health").handler(this::healthCheck);

        CompositeFuture.join(mongoFuture, redisFuture).setHandler(result -> {
            vertx.createHttpServer().requestHandler(router).listen(8080, ar -> {
                if (ar.succeeded()) {
                    logger.info("HTTP server listening on port {}", 8080);
                    startPromise.complete();
                } else {
                    logger.error("HTTP server fail to start {}", ar.cause().toString());
                    startPromise.fail(ar.cause());
                }
            });
        });
    }

    private void handleOrdersRequest(RoutingContext routingContext) {
        JsonObject incomingRequestData = routingContext.getBodyAsJson();
        logger.info("Order Request Received = " + incomingRequestData.toString());
        routingContext.put("apiName", "handleOrdersRequest");
        routingContext.put("startTime", System.currentTimeMillis());
        ordersService.createOrder(incomingRequestData, ResponseHandler.responseHandler(routingContext));
    }

    // health check can be implemented in several ways
    // health of hte backend services
    // health of databases etc...
    private void healthCheck(RoutingContext ctx) {
        logger.info("Health check");
        ctx.response().putHeader("Content-Type", "application/json").end(new JsonObject().put("status", "UP").encode());
    }

    private void getCORSHandler(RoutingContext routingContext) {
        String originPatterns = routingContext.request().getHeader(HttpHeaders.ORIGIN);
        routingContext.put(GatewayConstant.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        routingContext.put(GatewayConstant.ACCESS_CONTROL_ALLOW_ORIGIN, originPatterns);
        CorsHandler.create("/*")
                .allowedMethods(setCorsMethods())
                .allowedHeaders(setCorsHeaders())
                .exposedHeaders(setCorsExposedHeaders())
                .allowCredentials(true)
                .handle(routingContext);
    }

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new DeployVerticle());
    }

}
