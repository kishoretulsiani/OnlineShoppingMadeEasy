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
import org.shopping.common.components.utils.JsonUtility;
import org.shopping.company.common.testhelper.TestDataHelper;
import org.shopping.company.services.orders.helpers.DTOHelper;
import org.shopping.company.services.orders.helpers.DatabaseHelper;
import org.shopping.company.services.orders.helpers.RedisHelper;
import org.shopping.company.services.orders.request.CreateOrderRequest;
import org.shopping.company.services.orders.workflow.Context;

import java.util.concurrent.CompletableFuture;


@RunWith(JUnit4.class)
public class ConfirmAndUpdateItemsInventoryStepTest {

    DatabaseHelper databaseHelper;
    DTOHelper dtoHelper;
    RedisHelper redisHelper;
    ConfirmAndUpdateItemsInventoryStep confirmAndUpdateItemsInventoryStep;


    @Before
    public void setUp() throws Exception {
        databaseHelper = EasyMock.createNiceMock(DatabaseHelper.class);

        dtoHelper = new DTOHelper();

        redisHelper = EasyMock.createNiceMock(RedisHelper.class);

        confirmAndUpdateItemsInventoryStep = new ConfirmAndUpdateItemsInventoryStep(databaseHelper, dtoHelper, redisHelper);
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
        EasyMock.expect(redisHelper.updateInventory(EasyMock.anyObject(), EasyMock.anyInt()))
                .andReturn(CompletableFuture.completedFuture(Boolean.TRUE))
                .andReturn(CompletableFuture.completedFuture(Boolean.TRUE));
        EasyMock.expectLastCall().times(2);
        EasyMock.replay(redisHelper);

        CompletableFuture<Context> completableFuture = confirmAndUpdateItemsInventoryStep.execute(context);
        completableFuture.join();
        Assert.assertTrue(completableFuture.isDone());

    }
}
