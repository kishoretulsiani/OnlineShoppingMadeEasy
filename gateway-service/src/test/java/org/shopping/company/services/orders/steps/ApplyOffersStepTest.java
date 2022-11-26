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

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;


@RunWith(JUnit4.class)
public class ApplyOffersStepTest {

    DatabaseHelper databaseHelper;
    DTOHelper dtoHelper;
    RedisHelper redisHelper;
    ApplyOffersStep applyOffersStep;


    @Before
    public void setUp() throws Exception {
        databaseHelper = EasyMock.createNiceMock(DatabaseHelper.class);

        dtoHelper = new DTOHelper();

        redisHelper = EasyMock.createNiceMock(RedisHelper.class);

        applyOffersStep = new ApplyOffersStep(databaseHelper, dtoHelper, redisHelper);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void active_offers_applied_successfully_test() {
        Context context = new Context();
        JsonObject createOrderRequest = TestDataHelper.readCreateOrderRequestFromFile("create-order-success-request.json");
        context.setRequestObject(createOrderRequest);
        String strRequest = Json.encode(createOrderRequest);
        CreateOrderRequest createOrderRequest1 = JsonUtility.getInstance().getObject(strRequest, CreateOrderRequest.class);
        context.setCreateOrderRequest(createOrderRequest1);
        Order order = new Order();
        dtoHelper.createOrderDetails(order);
        context.setOrder(order);
        context.getOrder().setOrderItems(TestDataHelper.getDBOrderItemList());
        context.getOrder().setOrderAmountSummary(TestDataHelper.getOrderAmountSummaryWithoutDiscounts());
        dtoHelper.updateOrderQuantity(createOrderRequest1.getItemDetails(), context.getOrder().getOrderItems());


        HashMap<String, String> offersCache = new HashMap<>();
        offersCache.put("item1", "BUY_ONE_GET_ONE_FREE");
        offersCache.put("item2", "3_FOR_THE_PRICE_OF_2_ON_ORANGES");
        CompletableFuture.completedFuture(offersCache);
        EasyMock.expect(databaseHelper.getOffersCache())
                .andReturn(CompletableFuture.completedFuture(offersCache));
        EasyMock.replay(databaseHelper);

        CompletableFuture<Context> completableFuture = applyOffersStep.execute(context);


        Assert.assertNotNull(completableFuture.join().getOrder().getOrderAmountSummary());
        Assert.assertEquals("1.1", completableFuture.join().getOrder().getOrderAmountSummary().getOrderSubTotalAmount());
        Assert.assertEquals("10.0", completableFuture.join().getOrder().getOrderAmountSummary().getShippingAmount());
        Assert.assertEquals("11.27", completableFuture.join().getOrder().getOrderAmountSummary().getGrandTotal());
        Assert.assertEquals("0.85", completableFuture.join().getOrder().getOrderAmountSummary().getTotalDiscount());
        Assert.assertEquals("0.17", completableFuture.join().getOrder().getOrderAmountSummary().getTotalTax());


        Assert.assertTrue(completableFuture.isDone());

    }

    @Test
    public void active_offers_discounts_not_expected_test() {
        Context context = new Context();
        JsonObject createOrderRequest = TestDataHelper.readCreateOrderRequestFromFile("create-order-success-request.json");
        context.setRequestObject(createOrderRequest);
        String strRequest = Json.encode(createOrderRequest);
        CreateOrderRequest createOrderRequest1 = JsonUtility.getInstance().getObject(strRequest, CreateOrderRequest.class);
        context.setCreateOrderRequest(createOrderRequest1);
        Order order = new Order();
        dtoHelper.createOrderDetails(order);
        context.setOrder(order);
        context.getOrder().setOrderItems(TestDataHelper.getDBOrderItemList());
        context.getOrder().setOrderAmountSummary(TestDataHelper.getOrderAmountSummaryWithoutDiscounts());
        dtoHelper.updateOrderQuantity(createOrderRequest1.getItemDetails(), context.getOrder().getOrderItems());


        HashMap<String, String> offersCache = new HashMap<>();
        offersCache.put("item1", "BUY_ONE_GET_ONE_FREE");
        offersCache.put("item2", "3_FOR_THE_PRICE_OF_2_ON_ORANGES");
        CompletableFuture.completedFuture(offersCache);
        EasyMock.expect(databaseHelper.getOffersCache())
                .andReturn(CompletableFuture.completedFuture(offersCache));
        EasyMock.replay(databaseHelper);

        CompletableFuture<Context> completableFuture = applyOffersStep.execute(context);


        Assert.assertNotNull(completableFuture.join().getOrder().getOrderAmountSummary());
        Assert.assertFalse("1.11".equals(completableFuture.join().getOrder().getOrderAmountSummary().getOrderSubTotalAmount()));
        Assert.assertFalse("10.1".equals(completableFuture.join().getOrder().getOrderAmountSummary().getShippingAmount()));
        Assert.assertFalse("11.28".equals(completableFuture.join().getOrder().getOrderAmountSummary().getGrandTotal()));
        Assert.assertFalse("0.86".equals(completableFuture.join().getOrder().getOrderAmountSummary().getTotalDiscount()));
        Assert.assertFalse("0.18".equals(completableFuture.join().getOrder().getOrderAmountSummary().getTotalTax()));


        Assert.assertTrue(completableFuture.isDone());

    }


}
