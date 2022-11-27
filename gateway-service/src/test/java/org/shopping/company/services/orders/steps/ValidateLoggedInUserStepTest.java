package org.shopping.company.services.orders.steps;

import io.vertx.core.json.JsonObject;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.shopping.common.components.constants.ServiceAlerts;
import org.shopping.common.components.exception.ApplicationException;
import org.shopping.company.common.testhelper.TestDataHelper;
import org.shopping.company.services.orders.helpers.DTOHelper;
import org.shopping.company.services.orders.helpers.DatabaseHelper;
import org.shopping.company.services.orders.helpers.RedisHelper;
import org.shopping.company.services.orders.workflow.Context;
import org.shopping.datamodel.beans.ApplicationUser;

import java.util.concurrent.CompletableFuture;

@RunWith(JUnit4.class)
public class ValidateLoggedInUserStepTest {

    DatabaseHelper databaseHelper;
    DTOHelper dtoHelper;
    RedisHelper redisHelper;
    ValidateLoggedInUserStep validateLoggedInUserStep;


    @Before
    public void setUp() throws Exception {
        databaseHelper = EasyMock.createNiceMock(DatabaseHelper.class);

        dtoHelper = EasyMock.createNiceMock(DTOHelper.class);

        redisHelper = EasyMock.createNiceMock(RedisHelper.class);

        validateLoggedInUserStep = new ValidateLoggedInUserStep(databaseHelper, dtoHelper, redisHelper);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void request_for_valid_user_test() {
        Context context = new Context();
        JsonObject createOrderRequest = TestDataHelper.readCreateOrderRequestFromFile("create-order-success-request.json");
        context.setRequestObject(createOrderRequest);


        CompletableFuture<ApplicationUser> response = CompletableFuture.completedFuture(TestDataHelper.getApplicationUser());
        EasyMock.expect(databaseHelper.getApplicationUser(EasyMock.anyString())).andReturn(response);
        EasyMock.replay(databaseHelper);
        CompletableFuture<Context> completableFuture = validateLoggedInUserStep.execute(context);
        completableFuture.join();
        Assert.assertTrue(completableFuture.isDone());

    }

    @Test
    public void user_does_not_exists_in_users_db_test() {
        Context context = new Context();
        JsonObject createOrderRequest = TestDataHelper.readCreateOrderRequestFromFile("create-order-success-request.json");
        context.setRequestObject(createOrderRequest);

        EasyMock.expect(databaseHelper.getApplicationUser(EasyMock.anyString()))
                .andReturn(CompletableFuture.failedFuture(new ApplicationException(ServiceAlerts.INTERNAL_ERROR.getAlertCode(), ServiceAlerts.INTERNAL_ERROR.getAlertMessage(), null)));
        EasyMock.replay(databaseHelper);
        CompletableFuture<Context> completableFuture = validateLoggedInUserStep.execute(context).exceptionally(throwable -> {
            Assert.assertTrue(throwable instanceof ApplicationException);
            return null;
        });
        completableFuture.join();
        Assert.assertTrue(completableFuture.isDone());

    }

    @Test
    public void input_request_is_empty_test() {
        Context context = new Context();
        context.setRequestObject(new JsonObject());

        CompletableFuture<ApplicationUser> response = CompletableFuture.completedFuture(TestDataHelper.getApplicationUser());
        EasyMock.expect(databaseHelper.getApplicationUser(EasyMock.anyString())).andReturn(response);
        EasyMock.replay(databaseHelper);
        CompletableFuture<Context> completableFuture = validateLoggedInUserStep.execute(context).exceptionally(throwable -> {
            Assert.assertTrue(throwable instanceof ApplicationException);
            return null;
        });
        completableFuture.join();
        Assert.assertTrue(completableFuture.isDone());

    }
}
