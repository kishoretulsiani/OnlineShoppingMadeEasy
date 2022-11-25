package org.shopping.company.common.testhelper;


import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.shopping.company.gateway.constants.GatewayConstant;
import org.shopping.company.gateway.verticle.DeployVerticle;

public class TestResponseHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeployVerticle.class);

    public static <T> Handler<AsyncResult<T>> responseHandler(JsonObject serviceResponse) {

        return ar -> {

            if (ar.succeeded()) {
                logger.info("Success Response Received From Workflow");
                JsonObject jsonObject = (JsonObject) ar.result();

                String responsePayload = jsonObject.getString(GatewayConstant.RESPONSE_PAYLOAD);

                serviceResponse.put(GatewayConstant.RESPONSE_PAYLOAD, jsonObject);


            } else {
                logger.info("Failure Response Received From Workflow");
                serviceResponse.put(GatewayConstant.RESPONSE_PAYLOAD, ar.cause());
            }

        };
    }
}
