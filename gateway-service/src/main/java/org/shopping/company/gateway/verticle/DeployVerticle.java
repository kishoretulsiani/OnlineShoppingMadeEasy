package org.shopping.company.gateway.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.Map;


public class DeployVerticle extends AbstractVerticle {

    public DeployVerticle() {
    }

    private static final Logger logger = LoggerFactory.getLogger(DeployVerticle.class);

    private final HashMap<String, String> data = new HashMap<>();


    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        Map<String, String> env = System.getenv();

        data.put("key1", "value1");
        data.put("key2", "value2");
        data.put("key3", "value3");
        data.put("key4", "value4");

        int httpPort = Integer.parseInt(env.getOrDefault("HTTP_PORT", "8080"));

        Router router = Router.router(vertx);
        router.get("/data").handler(this::handleRequest);
        router.get("/health").handler(this::healthCheck);

        vertx.createHttpServer().requestHandler(router).listen(httpPort, ar -> {
            if (ar.succeeded()) {
                logger.info("HTTP server listening on port {}", httpPort);
                startPromise.complete();
            } else {
                startPromise.fail(ar.cause());
            }
        });
    }

    private void handleRequest(RoutingContext ctx) {

        logger.info("Request Received");

        JsonArray entries = new JsonArray();
        for (String key : data.keySet()) {
            entries.add(data.get(key));
        }
        JsonObject payload = new JsonObject().put("data", entries);
        ctx.response().putHeader("Content-Type", "application/json").end(payload.encode());
    }

    private final JsonObject okStatus = new JsonObject().put("status", "UP");

    private void healthCheck(RoutingContext ctx) {
        logger.info("Health check");
        ctx.response().putHeader("Content-Type", "application/json").end(okStatus.encode());
    }

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new DeployVerticle());
    }

}
