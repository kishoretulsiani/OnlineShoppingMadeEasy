package org.shopping.company.gateway.verticle;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.TimeoutHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shopping.common.components.constants.ResponseStatusMapper;
import org.shopping.common.components.mongo.MongoDB;
import org.shopping.common.components.redis.RedisCache;
import org.shopping.company.common.testhelper.TestDataHelper;
import org.shopping.company.gateway.dummydata.InsertDummyDataMongoDB;
import org.shopping.company.gateway.dummydata.InsertDummyDataRedis;
import org.shopping.company.gateway.handler.ExceptionHandler;
import org.shopping.company.gateway.handler.ResponseHandler;
import org.shopping.company.services.orders.OrdersService;

import java.util.Map;

@RunWith(VertxUnitRunner.class)
public class DeployVerticleTest {

    private static final Logger logger = LoggerFactory.getLogger(DeployVerticle.class);

    private static final String API_CONTEXT_ROOT = "/shopping/company/*";

    private static final String BASE_PATH = "/shopping/company/";

    private static final String ORDERS_API = "orders";

    private static OrdersService ordersService = null;

    private HttpClient httpClient;

    private static void handleOrdersRequest(RoutingContext routingContext) {
        JsonObject incomingRequest = routingContext.getBodyAsJson();
        MultiMap headers = routingContext.request().headers();
        logger.info("Order Request Received = " + incomingRequest.toString());
        routingContext.put("apiName", "handleOrdersRequest");
        routingContext.put("startTime", System.currentTimeMillis());
        ordersService.createOrder(incomingRequest, ResponseHandler.responseHandler(routingContext));
    }

    @Before
    public void setUp(TestContext context) throws Exception {

        Async async = context.async();

        Vertx vertx = Vertx.vertx();

        Map<String, String> env = System.getenv();

        // initializing MongoDB
        Future mongoFuture = MongoDB.initialize(vertx).compose(unused -> InsertDummyDataMongoDB.execute(vertx));

        // initializing Redis
        Future redisFuture = RedisCache.initialize(vertx).compose(unused -> InsertDummyDataRedis.execute(vertx));

        // initializing OrderService
        OrdersService.register(vertx);
        ordersService = OrdersService.createProxy(vertx);

        ResponseStatusMapper responseStatusMapper = new ResponseStatusMapper();

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route().handler(TimeoutHandler.create(8000, 501));
        router.route(API_CONTEXT_ROOT).failureHandler(new ExceptionHandler(responseStatusMapper));
//        router.route().handler(this::getCORSHandler);
        router.post(BASE_PATH + ORDERS_API).handler(DeployVerticleTest::handleOrdersRequest);
//        router.get("/health").handler(this::healthCheck);

        // Starting httpserver after core components are initialized successfully.
        CompositeFuture.join(mongoFuture, redisFuture).setHandler(result -> {
            if (result.succeeded()) {
                vertx.createHttpServer().requestHandler(router).listen(8080, ar -> {
                    if (ar.succeeded()) {
                        logger.info("HTTP server listening on port {}", 8080);
                        async.complete();
                    } else {
                        logger.error("HTTP server fail to start {}", ar.cause().toString());
                        context.fail(ar.cause());
                    }
                });
            } else {
                logger.error("error initializing core components", result.cause().toString());
            }
        });
    }

    @Test
    public void testServerUserRegister(TestContext context) {
        JsonObject createOrderRequest = TestDataHelper.readCreateOrderRequestFromFile("create-order-success-request.json");
        // Get an async object to control the completion of the test
        Async async = context.async();

        HttpClient client = Vertx.vertx().createHttpClient();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setURI("/shopping/company/orders");
        requestOptions.setHost("localhost");
        requestOptions.setPort(8080);
        requestOptions.setSsl(false);

        HttpClientRequest request = client.request(HttpMethod.POST, requestOptions, response -> {
            response.bodyHandler(buffer -> {
                logger.info(buffer.toString());
            });
            logger.info("response code" + response.statusCode());
            async.complete();
        });

        String body = Json.encode(createOrderRequest);
        Buffer writeBuffer = Buffer.buffer(body);
        request.headers().add("Content-Length", String.valueOf(writeBuffer.length()));
        request.putHeader("x-tracking-id", "7552916d-a1a4-41ed-9fc2-5c5e706015cf");
        request.putHeader("Content-Type", "application/json");
        request.write(body);
        request.end();
    }


}
