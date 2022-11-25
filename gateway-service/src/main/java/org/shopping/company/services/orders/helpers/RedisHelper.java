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
                    itemInventoryIfExistsFuture.completeExceptionally(new ApplicationException(ServiceAlerts.ITEM_OUT_OF_STOCK.getAlertCode(), ServiceAlerts.ITEM_OUT_OF_STOCK.getAlertMessage(), null));
                } else {
                    itemInventoryIfExistsFuture.complete(itemInventory);
                }
            } else {
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
                updateInventoryFuture.complete(false);
            }
        });
        return updateInventoryFuture;
    }
}
