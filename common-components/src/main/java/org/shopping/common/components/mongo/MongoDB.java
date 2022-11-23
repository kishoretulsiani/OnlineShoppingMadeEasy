package org.shopping.common.components.mongo;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import org.shopping.common.components.redis.RedisCache;

import java.util.concurrent.atomic.AtomicInteger;

public class MongoDB {

    private static final Logger logger = LoggerFactory.getLogger(RedisCache.class);

    private static MongoClient mongoClient = null;

    private static AtomicInteger clientStatus = new AtomicInteger(0); // 0 - not initialized, 1 - in-progress

    private MongoDB() {

    }

    public static synchronized Future<Void> initialize(Vertx vertx) {

        Promise<Void> promise = Promise.promise();

        if (clientStatus.compareAndSet(0, 1)) {

            if (mongoClient == null) {
                JsonObject mongoconfig = new JsonObject()
                        .put("connection_string", "mongodb://localhost:27017")
                        .put("db_name", "fruits_shop")
                        .put("username", "admin")
                        .put("password", "admin");

                mongoClient = MongoClient.create(vertx, mongoconfig);

                mongoClient.runCommand("ping", new JsonObject().put("ping", 1), result -> {
                    if (result.succeeded()) {
                        logger.warn("Mongo initialized successfully....");
                        promise.complete();
                    } else {
                        logger.warn("Mongo initialization failed");
                        promise.fail("Mongo initialization failed");
                    }
                });

            }
        }

        return promise.future();
    }

    public static MongoClient getClient() {
        if (mongoClient != null) {
            return mongoClient;
        } else {
            throw new RuntimeException("Mongo Client not Initialized.");
        }
    }
}
