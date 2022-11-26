package org.shopping.company.common.testhelper;


import io.vertx.core.json.JsonObject;
import org.shopping.datamodel.beans.DocumentType;
import org.shopping.datamodel.beans.OrderAmountSummary;
import org.shopping.datamodel.beans.OrderItem;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class TestDataHelper {

    private static String readFile(String fileName) {

        String fileContent = null;

        URL resource = TestDataHelper.class.getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! ");
        }
        File file = null;
        try {
            file = new File(resource.toURI());
            fileContent = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
        return fileContent;
    }

    public static JsonObject readCreateOrderRequestFromFile(String fileName) {
        String fileContents = readFile(fileName);
//        CreateOrderRequest createOrderRequest = JsonUtility.getInstance().getObject(fileContents, CreateOrderRequest.class);
        return new JsonObject(fileContents);
    }


    public static List<OrderItem> getDBOrderItemList() {
        List<OrderItem> orderItemList = new ArrayList<>();
        OrderItem orderItem1 = new OrderItem();
        orderItem1.setItemId("item1");
        orderItem1.setItemName("Apples");
        orderItem1.setItemDescription("Gala Apples");
        orderItem1.setItemPrice(0.60);
        orderItem1.setItemQuantity("0");
        orderItem1.setDocType(DocumentType.ORDER_ITEM);
        orderItemList.add(orderItem1);

        OrderItem orderItem2 = new OrderItem();
        orderItem2.setItemId("item2");
        orderItem2.setItemName("Oranges");
        orderItem2.setItemDescription("Navel Oranges");
        orderItem2.setItemPrice(0.25);
        orderItem2.setItemQuantity("0");
        orderItem2.setDocType(DocumentType.ORDER_ITEM);
        orderItemList.add(orderItem2);


        return orderItemList;
    }

    public static OrderAmountSummary getOrderAmountSummaryWithoutDiscounts() {
        OrderAmountSummary orderAmountSummary = new OrderAmountSummary();
        orderAmountSummary.setOrderSubTotalAmount("1.95");
        orderAmountSummary.setShippingAmount("10.0");
        orderAmountSummary.setGrandTotal("12.24");
        orderAmountSummary.setTotalDiscount(null);
        orderAmountSummary.setTotalTax("0.29");
        return orderAmountSummary;
    }


}
