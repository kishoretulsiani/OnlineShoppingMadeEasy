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
import org.shopping.datamodel.beans.Order;
import org.shopping.datamodel.beans.OrderItem;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RunWith(JUnit4.class)
public class CalculateOrderAmountsStepTest {

    DatabaseHelper databaseHelper;
    DTOHelper dtoHelper;
    RedisHelper redisHelper;
    CalculateOrderAmountsStep calculateOrderAmountsStep;


    @Before
    public void setUp() throws Exception {
        databaseHelper = EasyMock.createNiceMock(DatabaseHelper.class);

        dtoHelper = new DTOHelper();

        redisHelper = EasyMock.createNiceMock(RedisHelper.class);

        calculateOrderAmountsStep = new CalculateOrderAmountsStep(databaseHelper, dtoHelper, redisHelper);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void calculate_order_amount_step_success_test() {
        Context context = new Context();
        JsonObject createOrderRequest = TestDataHelper.readCreateOrderRequestFromFile("create-order-success-request.json");
        context.setRequestObject(createOrderRequest);
        String strRequest = Json.encode(createOrderRequest);
        CreateOrderRequest createOrderRequest1 = JsonUtility.getInstance().getObject(strRequest, CreateOrderRequest.class);
        context.setCreateOrderRequest(createOrderRequest1);
        Order order = new Order();
        dtoHelper.createOrderDetails(order);
        context.setOrder(order);

        CompletableFuture<List<OrderItem>> dbOrderItemList = CompletableFuture.completedFuture(TestDataHelper.getDBOrderItemList());
        EasyMock.expect(databaseHelper.getOrderItems(EasyMock.anyObject())).andReturn(dbOrderItemList);
        CompletableFuture<Boolean> dbUpdateOrder = CompletableFuture.completedFuture(Boolean.TRUE);
        EasyMock.expect(databaseHelper.orderCreateUpsert(EasyMock.anyObject(), EasyMock.anyObject())).andReturn(dbUpdateOrder);
        EasyMock.replay(databaseHelper);

        CompletableFuture<Context> completableFuture = calculateOrderAmountsStep.execute(context);
        completableFuture.join();
        Assert.assertTrue(completableFuture.isDone());

    }
}
