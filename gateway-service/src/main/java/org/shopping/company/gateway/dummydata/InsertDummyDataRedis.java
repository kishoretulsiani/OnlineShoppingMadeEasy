package org.shopping.company.gateway.dummydata;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.shopping.common.components.redis.RedisCache;

public class InsertDummyDataRedis {

    private static final Logger logger = LoggerFactory.getLogger(InsertDummyDataRedis.class);

    private InsertDummyDataRedis() {
    }

    public static synchronized Future<Void> execute(Vertx vertx) {

        Promise<Void> dataCreationPromise = Promise.promise();

        Promise item1Promise = Promise.promise();
        Promise item2Promise = Promise.promise();

        RedisCache.getClient()
                .set("item1", "100", result -> {
                    if (result.succeeded()) {
                        logger.info(" item1 Quantity Set in Redis");
                        item1Promise.complete();
                    } else {
                        logger.error(" Failed to set item1 Quantity ");
                        item1Promise.fail("Failed to initialize item1 quantity");
                    }
                });

        RedisCache.getClient()
                .set("item2", "100", result -> {
                    if (result.succeeded()) {
                        logger.info(" item2 Quantity Set in Redis");
                        item2Promise.complete();
                    } else {
                        logger.error(" Failed to set items Quantity ");
                        item2Promise.fail("Failed to initialize item1 quantity");
                    }

                });

        CompositeFuture.join(item1Promise.future(), item2Promise.future()).setHandler(result -> {
            if (result.succeeded()) {
                logger.info("Successfully initialized  items quantity in Redis");
                dataCreationPromise.complete();
            } else {
                dataCreationPromise.fail("Failed to initialize item1 quantity");
            }
        });

        return dataCreationPromise.future();
    }


}
