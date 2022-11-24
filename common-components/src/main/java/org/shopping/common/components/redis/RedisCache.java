package org.shopping.common.components.redis;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

import java.util.concurrent.atomic.AtomicInteger;

public class RedisCache {

    private static final Logger logger = LoggerFactory.getLogger(RedisCache.class);


    private static RedisClient redisClient = null;

    private static AtomicInteger clientStatus = new AtomicInteger(0); // 0 - not initialized, 1 - in-progress

    private RedisCache() {

    }

    public static synchronized Future<Void> initialize(Vertx vertx) {

        Promise<Void> promise = Promise.promise();

        if (clientStatus.compareAndSet(0, 1)) {

            if (redisClient == null) {

                String host = "127.0.0.1";

                redisClient = RedisClient.
                        create(vertx, new RedisOptions().setHost(host));

                redisClient.ping(stringAsyncResult -> {
                    if (stringAsyncResult.succeeded()) {
                        logger.warn("Redis initialized successfully....");
                        promise.complete();
                    } else {
                        logger.warn("Redis initialization failed");
                        promise.fail("Redis initialization failed");
                    }
                });
            }
        }
        return promise.future();
    }

    public static RedisClient getClient() {
        if (redisClient != null) {
            return redisClient;
        } else {
            throw new RuntimeException("Redis Client not Initialized.");
        }
    }

}
