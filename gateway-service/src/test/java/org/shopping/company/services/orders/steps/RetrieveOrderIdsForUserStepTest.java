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
public class RetrieveOrderIdsForUserStepTest {

    DatabaseHelper databaseHelper;
    DTOHelper dtoHelper;
    RedisHelper redisHelper;
    RetrieveOrderIdsForUserStep retrieveOrderIdsForUserStep;


    @Before
    public void setUp() throws Exception {
        databaseHelper = EasyMock.createNiceMock(DatabaseHelper.class);

        dtoHelper = EasyMock.createNiceMock(DTOHelper.class);

        redisHelper = EasyMock.createNiceMock(RedisHelper.class);

        retrieveOrderIdsForUserStep = new RetrieveOrderIdsForUserStep(databaseHelper, dtoHelper, redisHelper);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    //TODO
    public void orderId_list_is_null_or_empty_test() {
        Assert.assertTrue(true);

    }

    @Test
    //TODO
    public void orderId_list_is_exists_test() {
        Assert.assertTrue(true);
    }


}
