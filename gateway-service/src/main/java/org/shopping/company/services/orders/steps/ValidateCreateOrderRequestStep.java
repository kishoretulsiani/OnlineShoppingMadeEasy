package org.shopping.company.services.orders.steps;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.shopping.common.components.constants.ServiceAlerts;
import org.shopping.common.components.exception.ApplicationException;
import org.shopping.common.components.utils.JsonUtility;
import org.shopping.company.services.orders.helpers.DTOHelper;
import org.shopping.company.services.orders.helpers.DatabaseHelper;
import org.shopping.company.services.orders.helpers.RedisHelper;
import org.shopping.company.services.orders.request.CreateOrderRequest;
import org.shopping.company.services.orders.workflow.Context;
import org.shopping.company.services.orders.workflow.WorkflowStep;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class ValidateCreateOrderRequestStep implements WorkflowStep {

    private final Logger logger = LoggerFactory.getLogger(ValidateCreateOrderRequestStep.class);

    DatabaseHelper databaseHelper;

    DTOHelper dtoHelper;

    RedisHelper redisHelper;

    public ValidateCreateOrderRequestStep(DatabaseHelper databaseHelper, DTOHelper dtoHelper, RedisHelper redisHelper) {
        this.databaseHelper = databaseHelper;
        this.dtoHelper = dtoHelper;
        this.redisHelper = redisHelper;
    }


    @Override
    public CompletableFuture<Context> execute(Context context) {
        logger.info("Workflow Step1 Executed");
        CompletableFuture<Context> validateCreateOrderRequestFuture = new CompletableFuture();
        ArrayList<String> errorArgs = new ArrayList<>();

        String strRequest = context.getRequestObject().toString();
        CreateOrderRequest createOrderRequest = null;

        if (StringUtils.isBlank(strRequest)) {
            errorArgs.add("missing_request_body");
        } else {
            createOrderRequest = JsonUtility.getInstance().getObject(strRequest, CreateOrderRequest.class);
            logger.info("Request got converted to object");

            if (StringUtils.isBlank(createOrderRequest.getShippingAddressId())) {
                errorArgs.add("missing_shipping_address");
            }
        }

        if (errorArgs.size() == 0) {
            context.setCreateOrderRequest(createOrderRequest);
            validateCreateOrderRequestFuture.complete(context);
        } else {
            validateCreateOrderRequestFuture.completeExceptionally(new ApplicationException(ServiceAlerts.MANDATORY_DATA_MISSING.getAlertCode(), errorArgs.toString(), null));
        }

        return validateCreateOrderRequestFuture;
    }
}
