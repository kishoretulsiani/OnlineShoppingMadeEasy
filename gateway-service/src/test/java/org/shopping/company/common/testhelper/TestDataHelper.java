package org.shopping.company.common.testhelper;


import io.vertx.core.json.JsonObject;
import org.shopping.company.services.orders.workflow.Context;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

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

    public static Context buildContext() {
        Context context = new Context();
        URL resource = TestDataHelper.class.getClassLoader().getResource("create-order-success-request.json");
        if (resource == null) {
            throw new IllegalArgumentException("file not found! ");
        }
        File file = null;
        try {
            file = new File(resource.toURI());
            String lines = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            JsonObject requestBody = new JsonObject(lines);
            context.setRequestObject(requestBody);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
        return context;
    }


}
