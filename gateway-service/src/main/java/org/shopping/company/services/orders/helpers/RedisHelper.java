package org.shopping.company.services.orders.helpers;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.shopping.common.components.constants.ServiceAlerts;
import org.shopping.common.components.exception.ApplicationException;
import org.shopping.common.components.redis.RedisCache;

import java.util.concurrent.CompletableFuture;

//TODO this class is crucial for running the successful business to avoid difficult situations
// the logic should be discussed and implemented accordingly.
public class RedisHelper {

    private static final Logger logger = LoggerFactory.getLogger(RedisHelper.class);

    public CompletableFuture<Integer> getItemInventoryIfExists(String itemId, String requestedQuantity) {
        logger.info("getting item inventory from Redis " + itemId);

        CompletableFuture<Integer> itemInventoryIfExistsFuture = new CompletableFuture();

        RedisCache.getClient().get(itemId, inventoryResult -> {

            if (inventoryResult.succeeded()) {

                Integer itemInventory = Integer.valueOf(inventoryResult.result());
                Integer itemQuantity = Integer.valueOf(requestedQuantity);

                logger.info("item id {}, Quantity Needed {}, Inventory available {} ", itemId, itemQuantity, itemInventory);

                if (itemInventory == 0 || itemInventory < itemQuantity) {
                    logger.info("throwing Out of Stock error for itemId = {}", itemId);
                    itemInventoryIfExistsFuture.completeExceptionally(new ApplicationException(ServiceAlerts.ITEM_OUT_OF_STOCK.getAlertCode(), ServiceAlerts.ITEM_OUT_OF_STOCK.getAlertMessage(), null));
                } else {
                    logger.info("OK to receive order for item {}  ", itemId);

                    itemInventoryIfExistsFuture.complete(itemInventory);
                }
            } else {
                logger.info("error occurred while getting item inventory for item {}  ", itemId);
                itemInventoryIfExistsFuture.completeExceptionally(new ApplicationException(ServiceAlerts.ITEM_OUT_OF_STOCK.getAlertCode(), ServiceAlerts.ITEM_OUT_OF_STOCK.getAlertMessage(), null));
            }

        });
        return itemInventoryIfExistsFuture;
    }

    public CompletableFuture<Boolean> updateInventory(String itemId, Integer newInventoryValue) {

        logger.info("Updating item inventory in redis" + itemId);

        CompletableFuture<Boolean> updateInventoryFuture = new CompletableFuture();

        RedisCache.getClient().set(itemId, String.valueOf(newInventoryValue), voidAsyncResult -> {
            if (voidAsyncResult.succeeded()) {
                logger.info("item inventory updated in redis for " + itemId);
                updateInventoryFuture.complete(true);
            } else {
                logger.info("could not update item inventory in redis for " + itemId);
                //TODO this is not a good state to be. as it might be the case where some of the items inventory got update
                // there should be a solid logic like retry, reprocess orders to avoid this situation.
                updateInventoryFuture.completeExceptionally(new ApplicationException(ServiceAlerts.INTERNAL_ERROR.getAlertCode(), ServiceAlerts.INTERNAL_ERROR.getAlertMessage(), null));
            }
        });
        return updateInventoryFuture;
    }
}
