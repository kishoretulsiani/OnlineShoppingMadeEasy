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
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.TimeoutHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
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
import java.util.UUID;

@RunWith(VertxUnitRunner.class)
public class DeployVerticleTest {

    private static final Logger logger = LoggerFactory.getLogger(DeployVerticle.class);

    private static final String API_CONTEXT_ROOT = "/shopping/company/*";

    private static final String BASE_PATH = "/shopping/company/";

    private static final String ORDERS_API = "orders";

    private static OrdersService ordersService = null;

    private static HttpClient client;

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();


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

        Vertx vertx = rule.vertx();

        Map<String, String> env = System.getenv();

        client = rule.vertx().createHttpClient();

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
    // validations in this can be simple but we can go exhaustive like
    // validate everything in database also got inserted correctly
    // once we got CONFIRMED order in response
    public void create_order_success_test(TestContext context) {
        JsonObject createOrderRequest = TestDataHelper.readCreateOrderRequestFromFile("create-order-success-request.json");
        // Get an async object to control the completion of the test
        Async async = context.async();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setURI("/shopping/company/orders");
        requestOptions.setHost("localhost");
        requestOptions.setPort(8080);
        requestOptions.setSsl(false);


        HttpClientRequest request = client.request(HttpMethod.POST, requestOptions, response -> {
            // confirming http status in case of successful scenario
            Assert.assertEquals(200, response.statusCode());
            response.bodyHandler(buffer -> {
                logger.info(buffer.toString());
                JsonObject jsonObject = buffer.toJsonObject();
                // confirming orderId got generated
                Assert.assertNull(jsonObject.getString("orderId"));
            });
            async.complete();
            logger.info("http response code " + response.statusCode());
        });

        String body = Json.encode(createOrderRequest);
        Buffer writeBuffer = Buffer.buffer(body);
        request.headers().add("Content-Length", String.valueOf(writeBuffer.length()));
        request.putHeader("x-tracking-id", UUID.randomUUID().toString());
        request.putHeader("Content-Type", "application/json");
        request.write(body);
        request.end();
    }

    @Test
    @Ignore
    // we can write various test cases like this as per api swagger
    public void mandatory_data_missing_test(TestContext context) {
        JsonObject createOrderRequest = TestDataHelper.readCreateOrderRequestFromFile("create-order-success-request.json");
        createOrderRequest.remove("shippingAddressId");
        // Get an async object to control the completion of the test
        Async async = context.async();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setURI("/shopping/company/orders");
        requestOptions.setHost("localhost");
        requestOptions.setPort(8080);
        requestOptions.setSsl(false);

        HttpClientRequest request = client.request(HttpMethod.POST, requestOptions, response -> {
            // confirming http status in case of successful scenario
            logger.info("http response code = " + response.statusCode());
            Assert.assertEquals(400, response.statusCode());

            response.bodyHandler(buffer -> {
                logger.info(buffer.toString());
                // confirming orderId got generated
                logger.info(buffer.toString());
            });
            async.complete();

        });

        String body = Json.encode(createOrderRequest);
        Buffer writeBuffer = Buffer.buffer(body);
        request.headers().add("Content-Length", String.valueOf(writeBuffer.length()));
        request.putHeader("x-tracking-id", UUID.randomUUID().toString());
        request.putHeader("Content-Type", "application/json");
        request.write(body);
        request.end();

    }


    @Test
    @Ignore
    // we can write various test cases like this as per api swagger
    public void unauthorized_user_test(TestContext context) {
        JsonObject createOrderRequest = TestDataHelper.readCreateOrderRequestFromFile("create-order-success-request.json");
        createOrderRequest.remove("userId");
        // Get an async object to control the completion of the test
        Async async = context.async();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setURI("/shopping/company/orders");
        requestOptions.setHost("localhost");
        requestOptions.setPort(8080);
        requestOptions.setSsl(false);

        HttpClientRequest request = client.request(HttpMethod.POST, requestOptions, response -> {
            // confirming http status in case of successful scenario
            logger.info("http response code = " + response.statusCode());
            Assert.assertEquals(401, response.statusCode());
            response.bodyHandler(buffer -> {
                logger.info(buffer.toString());
                // confirming orderId got generated
                logger.info(buffer.toString());
                async.complete();
            });
        });

        String body = Json.encode(createOrderRequest);
        Buffer writeBuffer = Buffer.buffer(body);
        request.headers().add("Content-Length", String.valueOf(writeBuffer.length()));
        request.putHeader("x-tracking-id", UUID.randomUUID().toString());
        request.putHeader("Content-Type", "application/json");
        request.write(body);
        request.end();
    }

}
