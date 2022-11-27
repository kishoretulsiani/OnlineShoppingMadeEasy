package org.shopping.company.services.orders.steps;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.shopping.company.services.orders.helpers.DTOHelper;
import org.shopping.company.services.orders.helpers.DatabaseHelper;
import org.shopping.company.services.orders.helpers.RedisHelper;

@RunWith(JUnit4.class)
public class UpdateApplicationUserStepTest {

    DatabaseHelper databaseHelper;
    DTOHelper dtoHelper;
    RedisHelper redisHelper;
    UpdateApplicationUserStep updateApplicationUserStep;


    @Before
    public void setUp() throws Exception {
        databaseHelper = EasyMock.createNiceMock(DatabaseHelper.class);

        dtoHelper = EasyMock.createNiceMock(DTOHelper.class);

        redisHelper = EasyMock.createNiceMock(RedisHelper.class);

        updateApplicationUserStep = new UpdateApplicationUserStep(databaseHelper, dtoHelper, redisHelper);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    //TODO
    public void user_profile_updated_with_orderId_success_test() {
        Assert.assertTrue(true);

    }

    @Test
    //TODO
    public void user_profile_updated_with_orderId_failure_test() {
        Assert.assertTrue(true);
    }


}
