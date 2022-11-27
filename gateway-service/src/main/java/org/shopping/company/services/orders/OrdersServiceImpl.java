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
import org.shopping.company.gateway.constants.GatewayConstant;
import org.shopping.company.services.orders.workflow.Context;
import org.shopping.company.services.orders.workflow.CreateOrderWorkflow;
import org.shopping.company.services.orders.workflow.RetrieveOrdersWorkflow;
import org.shopping.company.services.orders.workflow.WorkflowErrorResponseHelper;

public class OrdersServiceImpl implements OrdersService {

    private final Logger logger = LoggerFactory.getLogger(OrdersServiceImpl.class);

    public OrdersServiceImpl(Vertx vertx) {
    }

    @Override
    public void createOrder(JsonObject request, Handler<AsyncResult<JsonObject>> asyncResultHandler) {

        logger.info("createOrder Request Received");

        Context context = new Context();
        try {
            context.setRequestObject(request);
            CreateOrderWorkflow createOrderWorkflow = new CreateOrderWorkflow(context);
            createOrderWorkflow.execute()
                    .thenAccept(workflowContext -> {
                        logger.info("createOrder Processing Success Response");
                        JsonObject respJson = new JsonObject();
                        respJson.put(GatewayConstant.RESPONSE_PAYLOAD, context.getResponsePayload());
                        respJson.put(GatewayConstant.HTTP_STATUS, 200);
                        asyncResultHandler.handle(Future.succeededFuture(respJson));
                    })
                    .exceptionally(throwable -> {
                        asyncResultHandler.handle(
                                ServiceException.fail(
                                        ServiceAlerts.INTERNAL_ERROR.getAlertCode(),
                                        throwable.getMessage(),
                                        WorkflowErrorResponseHelper.getErrorDetails(throwable, context)
                                )
                        );
                        return null;
                    });
        } catch (Exception exception) {
            asyncResultHandler.handle(
                    ServiceException.fail(
                            ServiceAlerts.INTERNAL_ERROR.getAlertCode(),
                            exception.getMessage(),
                            WorkflowErrorResponseHelper.getErrorDetails(exception, context)
                    )
            );
        }
    }

    @Override
    public void retrieveOrders(JsonObject request, Handler<AsyncResult<JsonObject>> asyncResultHandler) {

        logger.info("retrieveOrders Request Received");

        Context context = new Context();
        try {
            context.setRequestObject(request);
            RetrieveOrdersWorkflow retrieveOrdersWorkflow = new RetrieveOrdersWorkflow(context);
            retrieveOrdersWorkflow.execute()
                    .thenAccept(workflowContext -> {
                        logger.info("retrieveOrders Processing Success Response");
                        JsonObject respJson = new JsonObject();
                        respJson.put(GatewayConstant.RESPONSE_PAYLOAD, context.getResponsePayload());
                        respJson.put(GatewayConstant.HTTP_STATUS, 200);
                        asyncResultHandler.handle(Future.succeededFuture(respJson));
                    })
                    .exceptionally(throwable -> {
                        asyncResultHandler.handle(
                                ServiceException.fail(
                                        ServiceAlerts.INTERNAL_ERROR.getAlertCode(),
                                        throwable.getMessage(),
                                        WorkflowErrorResponseHelper.getErrorDetails(throwable, context)
                                )
                        );
                        return null;
                    });
        } catch (Exception exception) {
            asyncResultHandler.handle(
                    ServiceException.fail(
                            ServiceAlerts.INTERNAL_ERROR.getAlertCode(),
                            exception.getMessage(),
                            WorkflowErrorResponseHelper.getErrorDetails(exception, context)
                    )
            );
        }
    }
}
