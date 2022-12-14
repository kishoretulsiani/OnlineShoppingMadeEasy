package org.shopping.company.services.orders.steps;

import io.vertx.core.json.JsonObject;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.shopping.common.components.exception.ApplicationException;
import org.shopping.company.common.testhelper.TestDataHelper;
import org.shopping.company.services.orders.helpers.DTOHelper;
import org.shopping.company.services.orders.helpers.DatabaseHelper;
import org.shopping.company.services.orders.helpers.RedisHelper;
import org.shopping.company.services.orders.workflow.Context;

import java.util.concurrent.CompletableFuture;


public class ValidateCreateOrderRequestStepTest {

    DatabaseHelper databaseHelper;
    DTOHelper dtoHelper;
    RedisHelper redisHelper;
    ValidateCreateOrderRequestStep validateCreateOrderRequestStep;

    @Before
    public void setUp() throws Exception {

        databaseHelper = EasyMock.createNiceMock(DatabaseHelper.class);

        dtoHelper = EasyMock.createNiceMock(DTOHelper.class);

        redisHelper = EasyMock.createNiceMock(RedisHelper.class);

        validateCreateOrderRequestStep = new ValidateCreateOrderRequestStep(databaseHelper, dtoHelper, redisHelper);
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void vailid_create_order_request_test() {
        Context context = new Context();
        JsonObject createOrderRequest = TestDataHelper.readCreateOrderRequestFromFile("create-order-success-request.json");
        context.setRequestObject(createOrderRequest);
        CompletableFuture<Context> completableFuture = validateCreateOrderRequestStep.execute(context);
        completableFuture.join();
        Assert.assertTrue(completableFuture.isDone());

    }

    @Test
    public void mandatory_data_missing_test() {
        Context context = new Context();
        JsonObject createOrderRequest = TestDataHelper.readCreateOrderRequestFromFile("create-order-success-request.json");
        context.setRequestObject(createOrderRequest);
        context.getRequestObject().remove("shippingAddressId");
        CompletableFuture<Context> completableFuture = validateCreateOrderRequestStep.execute(context).exceptionally(throwable -> {
            Assert.assertTrue(throwable instanceof ApplicationException);
            return null;
        });
        completableFuture.join();
    }

    @Test
    public void request_is_null_or_empty_test() {
        Context context = new Context();
        context.setRequestObject(new JsonObject());
        CompletableFuture<Context> completableFuture = validateCreateOrderRequestStep.execute(context).exceptionally(throwable -> {
            Assert.assertTrue(throwable instanceof ApplicationException);
            return null;
        });
        completableFuture.join();
    }
}
