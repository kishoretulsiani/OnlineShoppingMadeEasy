package org.shopping.company.services.orders.steps;

import io.vertx.core.json.JsonObject;
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

import static org.mockito.Mockito.mock;


public class ValidateCreateOrderRequestStepTest {

    DatabaseHelper databaseHelper = mock(DatabaseHelper.class);

    DTOHelper dtoHelper = mock(DTOHelper.class);

    RedisHelper redisHelper = mock(RedisHelper.class);

    ValidateCreateOrderRequestStep validateCreateOrderRequestStep = new ValidateCreateOrderRequestStep(databaseHelper, dtoHelper, redisHelper);

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void executeSuccess() {
        Context context = new Context();
        JsonObject createOrderRequest = TestDataHelper.readCreateOrderRequestFromFile("create-order-success-request.json");
        context.setRequestObject(createOrderRequest);
        CompletableFuture<Context> completableFuture = validateCreateOrderRequestStep.execute(context);
        completableFuture.join();
        Assert.assertTrue(completableFuture.isDone());

    }

    @Test
    public void executeFailure() {
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
}
