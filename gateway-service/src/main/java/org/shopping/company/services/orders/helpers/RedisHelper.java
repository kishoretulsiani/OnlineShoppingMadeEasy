package org.shopping.company.services.orders.helpers;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.shopping.common.components.constants.ServiceAlerts;
import org.shopping.common.components.exception.ApplicationException;
import org.shopping.common.components.redis.RedisCache;

import java.util.concurrent.CompletableFuture;

public class RedisHelper {

    private static final Logger logger = LoggerFactory.getLogger(RedisHelper.class);


    public CompletableFuture<Integer> getItemInventoryIfExists(String itemId, String requestedQuantity) {

        CompletableFuture<Integer> itemInventoryIfExistsFuture = new CompletableFuture();

        RedisCache.getClient().get(itemId, inventoryResult -> {

            if (inventoryResult.succeeded()) {

                Integer itemInventory = Integer.valueOf(inventoryResult.result());
                Integer itemQuantity = Integer.valueOf(requestedQuantity);

                if (itemInventory == 0 || itemInventory < itemQuantity) {
                    logger.info("throwing inventory error 1");
                    itemInventoryIfExistsFuture.completeExceptionally(new ApplicationException(ServiceAlerts.ITEM_OUT_OF_STOCK.getAlertCode(), ServiceAlerts.ITEM_OUT_OF_STOCK.getAlertMessage(), null));
                } else {
                    logger.info("get inventory success KKKK");

                    itemInventoryIfExistsFuture.complete(itemInventory);
                }
            } else {
                logger.info("throwing inventory error");
                itemInventoryIfExistsFuture.completeExceptionally(new ApplicationException(ServiceAlerts.ITEM_OUT_OF_STOCK.getAlertCode(), ServiceAlerts.ITEM_OUT_OF_STOCK.getAlertMessage(), null));
            }

        });
        return itemInventoryIfExistsFuture;
    }

    public CompletableFuture<Boolean> updateInventory(String itemId, Integer newInventoryValue) {

        CompletableFuture<Boolean> updateInventoryFuture = new CompletableFuture();

        RedisCache.getClient().set(itemId, String.valueOf(newInventoryValue), voidAsyncResult -> {
            if (voidAsyncResult.succeeded()) {
                logger.info("item inventory updated in redis" + itemId);
                updateInventoryFuture.complete(true);
            } else {
                //TODO this is not a good state to be. as it might be the case where some of the items inventory got update
                // there should be a solid logic like retry, reprocess to avoid this situation.
                updateInventoryFuture.completeExceptionally(new ApplicationException(ServiceAlerts.INTERNAL_ERROR.getAlertCode(), ServiceAlerts.INTERNAL_ERROR.getAlertMessage(), null));
            }
        });
        return updateInventoryFuture;
    }
}
