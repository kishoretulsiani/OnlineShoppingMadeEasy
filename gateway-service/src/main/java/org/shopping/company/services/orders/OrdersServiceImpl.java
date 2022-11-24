package org.shopping.company.services.orders;


import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.serviceproxy.ServiceException;
import org.shopping.common.components.constants.ServiceAlerts;
import org.shopping.common.components.utils.ErrorResponseHelper;
import org.shopping.common.components.workflow.Context;
import org.shopping.company.gateway.constants.GatewayConstant;
import org.shopping.company.services.orders.workflow.CreateOrderWorkflow;

public class OrdersServiceImpl implements OrdersService {

    private final Logger logger = LoggerFactory.getLogger(OrdersServiceImpl.class);

    public OrdersServiceImpl(Vertx vertx) {
    }

    @Override
    public void createOrder(JsonObject request, Handler<AsyncResult<JsonObject>> asyncResultHandler) {

        logger.info("OrdersServiceImpl Request Received");

        Context context = new Context();
        try {
            CreateOrderWorkflow createOrderWorkflow = new CreateOrderWorkflow(context);
            createOrderWorkflow.execute()
                    .thenAccept(workflowContext -> {
                        logger.info("OrdersServiceImpl Processing Success Response");
                        JsonObject respJson = new JsonObject();
                        respJson.put(GatewayConstant.RESPONSE_PAYLOAD, context.getResponsePayload());
                        respJson.put(GatewayConstant.HTTP_STATUS, 200);
                        asyncResultHandler.handle(Future.succeededFuture(respJson));
                    })
                    .exceptionally(throwable -> {
                        asyncResultHandler.handle(
                                ServiceException.fail(
                                        ServiceAlerts.INTERNAL_API_ERROR.getAlertCode(),
                                        throwable.getMessage(),
                                        ErrorResponseHelper.getErrorDetails(throwable, context)
                                )
                        );
                        return null;
                    });
        } catch (Exception exception) {
            asyncResultHandler.handle(
                    ServiceException.fail(
                            ServiceAlerts.INTERNAL_API_ERROR.getAlertCode(),
                            exception.getMessage(),
                            ErrorResponseHelper.getErrorDetails(exception, context)
                    )
            );
        }
    }
}
