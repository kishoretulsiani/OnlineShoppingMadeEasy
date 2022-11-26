package org.shopping.company.services.orders.workflow;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.shopping.common.components.constants.ServiceAlerts;
import org.shopping.common.components.exception.ApplicationException;

@RunWith(JUnit4.class)

public class WorkflowErrorResponseHelperTest {


    @Test
    public void workflow_response_due_to_application_exception_test() {
        Throwable throwable = new Exception("Some Workflow Exception", new ApplicationException(ServiceAlerts.INVALID_USER.getAlertCode(), ServiceAlerts.INVALID_USER.getAlertMessage(), null));
        JsonObject errorResponse = WorkflowErrorResponseHelper.getErrorDetails(throwable, new Context());
        Assert.assertNotNull(errorResponse);
        Assert.assertTrue(Integer.valueOf(ServiceAlerts.INVALID_USER.getAlertCode()).equals(errorResponse.getInteger("status_code")));
        Assert.assertEquals(ServiceAlerts.INVALID_USER.getAlertMessage(), errorResponse.getString("status_code_type"));
        Assert.assertNotNull(errorResponse.getString("detailed_error_trace"));
    }

    @Test
    public void workflow_response_due_to_random_exception_test() {
        JsonObject errorResponse = WorkflowErrorResponseHelper.getErrorDetails(new Exception(), new Context());
        Assert.assertNotNull(errorResponse);
        Assert.assertTrue(Integer.valueOf(ServiceAlerts.INTERNAL_ERROR.getAlertCode()).equals(errorResponse.getInteger("status_code")));
        Assert.assertEquals(ServiceAlerts.INTERNAL_ERROR.getAlertMessage(), errorResponse.getString("status_code_type"));
        Assert.assertNotNull(errorResponse.getString("detailed_error_trace"));
    }

}
