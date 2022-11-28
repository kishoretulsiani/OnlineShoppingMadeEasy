package org.shopping.company.services.orders.steps;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.shopping.common.components.constants.ServiceAlerts;
import org.shopping.common.components.exception.ApplicationException;
import org.shopping.company.services.orders.helpers.DTOHelper;
import org.shopping.company.services.orders.helpers.DatabaseHelper;
import org.shopping.company.services.orders.helpers.RedisHelper;
import org.shopping.company.services.orders.response.CreateOrderResponse;
import org.shopping.company.services.orders.workflow.Context;
import org.shopping.company.services.orders.workflow.WorkflowStep;
import org.shopping.datamodel.beans.ORDER_STATUS;

import java.util.concurrent.CompletableFuture;

public class CreateOrderResponseStep implements WorkflowStep {

    private final Logger logger = LoggerFactory.getLogger(CreateOrderResponseStep.class);

    DatabaseHelper databaseHelper;

    DTOHelper dtoHelper;

    RedisHelper redisHelper;

    public CreateOrderResponseStep(DatabaseHelper databaseHelper, DTOHelper dtoHelper, RedisHelper redisHelper) {
        this.databaseHelper = databaseHelper;
        this.dtoHelper = dtoHelper;
        this.redisHelper = redisHelper;
    }

    @Override
    public CompletableFuture<Context> execute(Context context) {
        logger.info("Executing CreateOrderResponseStep Step");
        CompletableFuture<Context> createOrderStepFuture = new CompletableFuture();

        CreateOrderResponse createOrderResponse = new CreateOrderResponse();

        dtoHelper.createShippingDetails(context.getOrder(), context);
        dtoHelper.createPaymentDetails(context.getOrder());

        context.getOrder().getOrderDetails().setOrderStatus(ORDER_STATUS.CONFIRMED);

        createOrderResponse.setOrder(context.getOrder());


        databaseHelper.orderCreateUpsert(context.getOrder(), context).thenAccept(orderUpdated -> {
            if (orderUpdated) {
                context.setCreateOrderResponse(createOrderResponse);
                logger.info("CreateOrderResponseStep Step completed");
                createOrderStepFuture.complete(context);
            } else {
                createOrderStepFuture.completeExceptionally(new ApplicationException(ServiceAlerts.INTERNAL_ERROR.getAlertCode(), ServiceAlerts.INTERNAL_ERROR.getAlertMessage(), null));
            }
        }).exceptionally(throwable -> {
            logger.info("error occurred in  CreateOrderResponseStep" + throwable.getMessage());
            createOrderStepFuture.completeExceptionally(new ApplicationException(ServiceAlerts.INTERNAL_ERROR.getAlertCode(), ServiceAlerts.INTERNAL_ERROR.getAlertMessage(), null));
            return null;
        });


        return createOrderStepFuture;
    }
}
