package org.shopping.company.gateway.dummydata;

import com.mongodb.MongoCommandException;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.BulkOperation;
import org.shopping.common.components.mongo.MongoDB;

import java.util.Arrays;
import java.util.List;

public class InsertDummyDataMongoDB {

    private static final Logger logger = LoggerFactory.getLogger(InsertDummyDataMongoDB.class);


    private InsertDummyDataMongoDB() {
    }

    public static synchronized Future<Void> execute(Vertx vertx) {

        Promise<Void> dataCreationPromise = Promise.promise();

        Promise<Void> userCollectionPromise = Promise.promise();

        MongoDB.getClient().createCollection("APPLICATION_USERS", res -> {
            if (res.succeeded()) {
                logger.info("APPLICATION_USERS Collection got created successfully");
            } else {
                if (res.cause() instanceof MongoCommandException) {
                    MongoCommandException exception = (MongoCommandException) res.cause();
                    if (exception.getCode() == 48) {
                        //ignore as collection already exists and no need to create a new one.
                        logger.info("APPLICATION_USERS Collection already exists");

                        userCollectionPromise.complete();
                    }
                } else {
                    res.cause().printStackTrace();
                    logger.info("APPLICATION_USERS Collection Creation Failed");
                    userCollectionPromise.fail("APPLICATION_USERSCollection Creation Failed");
                }
            }
        });

        Promise<Void> ordersCollectionPromise = Promise.promise();
        MongoDB.getClient().createCollection("ORDERS", res -> {
            if (res.succeeded()) {
                logger.info("Collection got created successfully");
                ordersCollectionPromise.complete();
            } else {
                if (res.cause() instanceof MongoCommandException) {
                    MongoCommandException exception = (MongoCommandException) res.cause();
                    if (exception.getCode() == 48) {
                        //ignore as collection already exists and no need to create a new one.
                        logger.info("ORDERS Collection already exists");

                        ordersCollectionPromise.complete();
                    }
                } else {
                    res.cause().printStackTrace();
                    logger.info("ORDERS Collection Creation Failed");
                    ordersCollectionPromise.fail("ORDERS Collection Creation Failed");
                }
            }
        });

        Promise<Void> orderItemsCollectionPromise = Promise.promise();
        MongoDB.getClient().createCollection("ORDER_ITEMS", res -> {
            if (res.succeeded()) {
                logger.info("Collection got created successfully");
                orderItemsCollectionPromise.complete();
            } else {
                if (res.cause() instanceof MongoCommandException) {
                    MongoCommandException exception = (MongoCommandException) res.cause();
                    if (exception.getCode() == 48) {
                        //ignore as collection already exists and no need to create a new one.
                        logger.info("APPLICATION_USERS Collection already exists");

                        orderItemsCollectionPromise.complete();
                    }
                } else {
                    res.cause().printStackTrace();
                    logger.info("ORDER_ITEMS Collection Creation Failed");
                    orderItemsCollectionPromise.fail("ORDER_ITEMS Collection Creation Failed");
                }
            }
        });


        Promise<Void> usersInsertPromise = Promise.promise();
        JsonObject user1 = new JsonObject()
                .put("userId", "userId1")
                .put("userEmail", "userId1@abc.com")
                .put("firstName", "firstName")
                .put("lastName", "lastName")
                .put("address", "null")
                .put("docType", "APPLICATION_USER");

        JsonObject user2 = new JsonObject()
                .put("userId", "userId2")
                .put("userEmail", "userId2@abc.com")
                .put("firstName", "firstName")
                .put("lastName", "lastName")
                .put("address", "null")
                .put("docType", "APPLICATION_USER");

        JsonObject filter1 = new JsonObject()
                .put("userId", "userId1");

        JsonObject filter2 = new JsonObject()
                .put("userId", "userId2");


        BulkOperation bulkReplace1 = BulkOperation.createReplace(filter1, user1).setUpsert(true);
        BulkOperation bulkReplace2 = BulkOperation.createReplace(filter2, user2).setUpsert(true);


        List<BulkOperation> operations = Arrays.asList(
                bulkReplace1, bulkReplace2);

        MongoDB.getClient().bulkWrite("APPLICATION_USERS", operations, user1Result -> {
            if (user1Result.succeeded()) {
                logger.info("user1 replaced !");
                usersInsertPromise.complete();
            } else {
                user1Result.cause().printStackTrace();
                usersInsertPromise.fail("APPLICATION_USERS Collection Initialization Failed");
            }
        });


        Promise<Void> itemsInsertPromise = Promise.promise();
        JsonObject item1 = new JsonObject()
                .put("itemId", "item1")
                .put("itemName", "Apples")
                .put("itemDescription", "Gala Apples")
                .put("itemPrice", 2)
                .put("itemQuantity", "0")
                .put("docType", "ORDER_ITEM");

        JsonObject item2 = new JsonObject()
                .put("itemId", "item2")
                .put("itemName", "Oranges")
                .put("itemDescription", "Naval Oranges")
                .put("itemPrice", 2)
                .put("itemQuantity", "0")
                .put("docType", "ORDER_ITEM");

        JsonObject itemFilter1 = new JsonObject()
                .put("itemId", "item1");

        JsonObject itemFilter2 = new JsonObject()
                .put("itemId", "item2");


        BulkOperation bulkReplaceItem1 = BulkOperation.createReplace(itemFilter1, item1).setUpsert(true);
        BulkOperation bulkReplaceItem2 = BulkOperation.createReplace(itemFilter2, item2).setUpsert(true);


        List<BulkOperation> itemOperations = Arrays.asList(
                bulkReplaceItem1, bulkReplaceItem2);

        MongoDB.getClient().bulkWrite("ORDER_ITEMS", itemOperations, itemResults -> {
            if (itemResults.succeeded()) {
                logger.info("user1 replaced !");
                itemsInsertPromise.complete();
            } else {
                itemResults.cause().printStackTrace();
                itemsInsertPromise.fail("APPLICATION_USERS Collection Initialization Failed");
            }
        });


        CompositeFuture.join(
                userCollectionPromise.future(),
                ordersCollectionPromise.future(),
                orderItemsCollectionPromise.future(),
                usersInsertPromise.future(),
                itemsInsertPromise.future()
        ).setHandler(result -> {
            if (result.succeeded()) {
                dataCreationPromise.complete();

            } else {
                dataCreationPromise.fail("data initialization failed due to " + result.cause().toString());
            }
        });


        return dataCreationPromise.future();
    }


}
