package org.shopping.company.services.orders.steps;

import io.vertx.core.json.Json;
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
import org.shopping.common.components.utils.JsonUtility;
import org.shopping.company.common.testhelper.TestDataHelper;
import org.shopping.company.services.orders.helpers.DTOHelper;
import org.shopping.company.services.orders.helpers.DatabaseHelper;
import org.shopping.company.services.orders.helpers.RedisHelper;
import org.shopping.company.services.orders.request.CreateOrderRequest;
import org.shopping.company.services.orders.workflow.Context;

import java.util.concurrent.CompletableFuture;


@RunWith(JUnit4.class)
public class ConfirmInventoryStepTest {

    DatabaseHelper databaseHelper;
    DTOHelper dtoHelper;
    RedisHelper redisHelper;
    ConfirmInventoryStep confirmInventoryStep;


    @Before
    public void setUp() throws Exception {
        databaseHelper = EasyMock.createNiceMock(DatabaseHelper.class);

        dtoHelper = new DTOHelper();

        redisHelper = EasyMock.createNiceMock(RedisHelper.class);

        confirmInventoryStep = new ConfirmInventoryStep(databaseHelper, dtoHelper, redisHelper);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void all_items_inventory_exists_test() {
        Context context = new Context();
        JsonObject createOrderRequest = TestDataHelper.readCreateOrderRequestFromFile("create-order-success-request.json");
        context.setRequestObject(createOrderRequest);
        String strRequest = Json.encode(createOrderRequest);
        CreateOrderRequest createOrderRequest1 = JsonUtility.getInstance().getObject(strRequest, CreateOrderRequest.class);
        context.setCreateOrderRequest(createOrderRequest1);


        EasyMock.expect(redisHelper.getItemInventoryIfExists(EasyMock.anyString(), EasyMock.anyString()))
                .andReturn(CompletableFuture.completedFuture(10))
                .andReturn(CompletableFuture.completedFuture(10));
        EasyMock.expectLastCall().times(2);
        EasyMock.replay(redisHelper);

        CompletableFuture<Context> completableFuture = confirmInventoryStep.execute(context);
        completableFuture.join();
        Assert.assertTrue(completableFuture.isDone());

    }

    @Test
    public void inventory_does_not_exists_for_all_items_in_request_test() {
        Context context = new Context();
        JsonObject createOrderRequest = TestDataHelper.readCreateOrderRequestFromFile("create-order-success-request.json");
        context.setRequestObject(createOrderRequest);
        String strRequest = Json.encode(createOrderRequest);
        CreateOrderRequest createOrderRequest1 = JsonUtility.getInstance().getObject(strRequest, CreateOrderRequest.class);
        context.setCreateOrderRequest(createOrderRequest1);

        EasyMock.expect(redisHelper.getItemInventoryIfExists(EasyMock.anyString(), EasyMock.anyString()))
                .andReturn(CompletableFuture.failedFuture(new ApplicationException(ServiceAlerts.ITEM_OUT_OF_STOCK.getAlertCode(), ServiceAlerts.ITEM_OUT_OF_STOCK.getAlertMessage(), null)))
                .andReturn(CompletableFuture.failedFuture(new ApplicationException(ServiceAlerts.ITEM_OUT_OF_STOCK.getAlertCode(), ServiceAlerts.ITEM_OUT_OF_STOCK.getAlertMessage(), null)));

        EasyMock.expectLastCall().times(2);
        EasyMock.replay(redisHelper);

        CompletableFuture<Context> completableFuture = confirmInventoryStep.execute(context);
        Assert.assertTrue(completableFuture.isCompletedExceptionally());
    }

    @Test
    public void inventory_does_not_exists_for_single_item_in_request_test() {
        Context context = new Context();
        JsonObject createOrderRequest = TestDataHelper.readCreateOrderRequestFromFile("create-order-success-request.json");
        context.setRequestObject(createOrderRequest);
        String strRequest = Json.encode(createOrderRequest);
        CreateOrderRequest createOrderRequest1 = JsonUtility.getInstance().getObject(strRequest, CreateOrderRequest.class);
        context.setCreateOrderRequest(createOrderRequest1);

        EasyMock.expect(redisHelper.getItemInventoryIfExists(EasyMock.anyString(), EasyMock.anyString()))
                .andReturn(CompletableFuture.completedFuture(10))
                .andReturn(CompletableFuture.failedFuture(new ApplicationException(ServiceAlerts.ITEM_OUT_OF_STOCK.getAlertCode(), ServiceAlerts.ITEM_OUT_OF_STOCK.getAlertMessage(), null)));

        EasyMock.expectLastCall().times(2);
        EasyMock.replay(redisHelper);

        CompletableFuture<Context> completableFuture = confirmInventoryStep.execute(context);
        Assert.assertTrue(completableFuture.isCompletedExceptionally());
    }
}
