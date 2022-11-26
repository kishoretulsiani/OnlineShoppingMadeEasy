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

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;


@RunWith(JUnit4.class)
public class UpdateItemsInventoryStepTest {

    DatabaseHelper databaseHelper;
    DTOHelper dtoHelper;
    RedisHelper redisHelper;
    UpdateItemsInventoryStep updateItemsInventoryStep;


    @Before
    public void setUp() throws Exception {
        databaseHelper = EasyMock.createNiceMock(DatabaseHelper.class);

        dtoHelper = new DTOHelper();

        redisHelper = EasyMock.createNiceMock(RedisHelper.class);

        updateItemsInventoryStep = new UpdateItemsInventoryStep(databaseHelper, dtoHelper, redisHelper);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void update_all_items_inventory_success_test() {
        Context context = new Context();
        JsonObject createOrderRequest = TestDataHelper.readCreateOrderRequestFromFile("create-order-success-request.json");
        context.setRequestObject(createOrderRequest);
        String strRequest = Json.encode(createOrderRequest);
        CreateOrderRequest createOrderRequest1 = JsonUtility.getInstance().getObject(strRequest, CreateOrderRequest.class);
        context.setCreateOrderRequest(createOrderRequest1);

        HashMap<String, Integer> itemInventory = new HashMap<>();
        itemInventory.put("item1", 10);
        itemInventory.put("item2", 10);
        context.setItemInventory(itemInventory);

        EasyMock.expect(redisHelper.updateInventory(EasyMock.anyString(), EasyMock.anyObject()))
                .andReturn(CompletableFuture.completedFuture(true))
                .andReturn(CompletableFuture.completedFuture(true));
        EasyMock.expectLastCall().times(2);

        EasyMock.replay(redisHelper);

        CompletableFuture<Context> completableFuture = updateItemsInventoryStep.execute(context);
        completableFuture.join();
        Assert.assertTrue(completableFuture.isDone());

    }

    @Test
    public void update_any_item_inventory_failed_test() {
        Context context = new Context();
        JsonObject createOrderRequest = TestDataHelper.readCreateOrderRequestFromFile("create-order-success-request.json");
        context.setRequestObject(createOrderRequest);
        String strRequest = Json.encode(createOrderRequest);
        CreateOrderRequest createOrderRequest1 = JsonUtility.getInstance().getObject(strRequest, CreateOrderRequest.class);
        context.setCreateOrderRequest(createOrderRequest1);


        HashMap<String, Integer> itemInventory = new HashMap<>();
        itemInventory.put("item1", 10);
        itemInventory.put("item2", 10);
        context.setItemInventory(itemInventory);

        EasyMock.expect(redisHelper.updateInventory(EasyMock.anyString(), EasyMock.anyObject()))
                .andReturn(CompletableFuture.failedFuture(new ApplicationException(ServiceAlerts.INTERNAL_ERROR.getAlertCode(), ServiceAlerts.INTERNAL_ERROR.getAlertMessage(), null)))
                .andReturn(CompletableFuture.completedFuture(true));
        EasyMock.expectLastCall().times(2);

        EasyMock.replay(redisHelper);

        CompletableFuture<Context> completableFuture = updateItemsInventoryStep.execute(context);
        Assert.assertTrue(completableFuture.isCompletedExceptionally());

    }


}
