package org.shopping.company.gateway.handler;


import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import org.shopping.company.gateway.constants.GatewayConstant;
import org.shopping.company.gateway.verticle.DeployVerticle;

public class ResponseHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeployVerticle.class);

    public static <T> Handler<AsyncResult<T>> responseHandler(RoutingContext routingContext) {

        return ar -> {
            String trackingId = routingContext.request().getHeader(GatewayConstant.TRACKING_ID);
            if (ar.succeeded()) {
                logger.info("Success Response Received From Workflow");
                JsonObject jsonObject = (JsonObject) ar.result();

                String responsePayload = jsonObject.getString(GatewayConstant.RESPONSE_PAYLOAD);

                routingContext.response()
                        .putHeader(GatewayConstant.X_TRACKING_ID, trackingId)
                        .putHeader(GatewayConstant.CONTENT_TYPE, GatewayConstant.APPLICATION_JSON)
                        .setStatusCode(jsonObject.getInteger(GatewayConstant.HTTP_STATUS) == null ? GatewayConstant.HTTP_STATUS_200 : jsonObject.getInteger(GatewayConstant.HTTP_STATUS));

                routingContext.response().end(responsePayload);

            } else {
                logger.info("Failure Response Received From Workflow");
                routingContext.fail(ar.cause());
            }
        };
    }
}
