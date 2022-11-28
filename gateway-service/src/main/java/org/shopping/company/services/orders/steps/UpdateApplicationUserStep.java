package org.shopping.company.services.orders.steps;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.shopping.common.components.constants.ServiceAlerts;
import org.shopping.common.components.exception.ApplicationException;
import org.shopping.company.services.orders.helpers.DTOHelper;
import org.shopping.company.services.orders.helpers.DatabaseHelper;
import org.shopping.company.services.orders.helpers.RedisHelper;
import org.shopping.company.services.orders.workflow.Context;
import org.shopping.company.services.orders.workflow.WorkflowStep;
import org.shopping.datamodel.beans.ApplicationUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UpdateApplicationUserStep implements WorkflowStep {

    private final Logger logger = LoggerFactory.getLogger(UpdateApplicationUserStep.class);

    DatabaseHelper databaseHelper;

    DTOHelper dtoHelper;

    RedisHelper redisHelper;

    public UpdateApplicationUserStep(DatabaseHelper databaseHelper, DTOHelper dtoHelper, RedisHelper redisHelper) {
        this.databaseHelper = databaseHelper;
        this.dtoHelper = dtoHelper;
        this.redisHelper = redisHelper;
    }

    @Override
    public CompletableFuture<Context> execute(Context context) {
        logger.info("Executing UpdateApplicationUserStep Step");
        CompletableFuture<Context> updateApplicationUserStepFuture = new CompletableFuture();

        ApplicationUser applicationUser = context.getLoggedInUser();
        List<String> orderIds = applicationUser.getOrdersIds();
        if (orderIds == null) {
            orderIds = new ArrayList<>();
            applicationUser.setOrdersIds(orderIds);
        }
        orderIds.add(context.getOrder().getOrderId());

        databaseHelper.updateApplicationUser(applicationUser, applicationUser.getUserId()).thenAccept(userUpdated -> {
            if (userUpdated) {
                logger.info("UpdateApplicationUserStep Step completed");
                updateApplicationUserStepFuture.complete(context);
            } else {
                updateApplicationUserStepFuture.completeExceptionally(new ApplicationException(ServiceAlerts.INTERNAL_ERROR.getAlertCode(), ServiceAlerts.INTERNAL_ERROR.getAlertMessage(), null));
            }
        }).exceptionally(throwable -> {
            logger.info("error occurred in  UpdateApplicationUserStep" + throwable.getMessage());
            updateApplicationUserStepFuture.completeExceptionally(new ApplicationException(ServiceAlerts.INTERNAL_ERROR.getAlertCode(), ServiceAlerts.INTERNAL_ERROR.getAlertMessage(), null));
            return null;
        });


        return updateApplicationUserStepFuture;
    }
}
