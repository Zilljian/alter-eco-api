package org.alter.eco.api.service.db;

import lombok.RequiredArgsConstructor;
import org.alter.eco.api.jooq.tables.Item;
import org.alter.eco.api.jooq.tables.Order;
import org.alter.eco.api.jooq.tables.records.ItemRecord;
import org.alter.eco.api.logic.shop.FindItemsOperation.FindItemsRequest;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ShopService {

    private static final Logger log = LoggerFactory.getLogger(ShopService.class);

    private final DSLContext db;

    private final Item itemTable = Item.ITEM;
    private final Order orderTable = Order.ORDER;

    public ItemRecord insert(ItemRecord item) {
        log.info("ShopService.insert.in item = {}", item);
        var result = db.insertInto(itemTable)
            .set(item)
            .returning(itemTable.ID)
            .fetchOne();
        log.info("ShopService.insert.out result = {}", result);
        return result;
    }

    public Optional<ItemRecord> findById(Long id) {
        log.info("ShopService.findById.in id = {}", id);
        var result = db.selectFrom(itemTable)
            .where(itemTable.ID.equal(id))
            .fetchOptional();
        log.info("ShopService.findById.out");
        return result;
    }

    public void update(ItemRecord forUpdate) {
        log.info("ShopService.update.in forUpdate = {}", forUpdate);
        db.update(itemTable)
            .set(forUpdate)
            .where(itemTable.ID.equal(forUpdate.getId()))
            .execute();
        log.info("ShopService.update.out");
    }

    public List<ItemRecord> findByFilters(FindItemsRequest request) {
        log.info("ShopService.findByFilters.in request = {}", request);
        var result = List.of(
            request.withCondition(db.selectFrom(itemTable))
                .fetchArray()
        );
        log.info("ShopService.findByFilters.out result = {}", result);
        return result;
    }

    public void purchaseItem(String userUuid, Long itemId) {
        log.info("ShopService.purchaseItem.in itemId = {}", itemId);
        db.insertInto(orderTable)
            .set(orderTable.CUSTOMER, userUuid)
            .set(orderTable.ITEM_ID, itemId)
            .execute();
        log.info("ShopService.purchaseItem.out");
    }

    public List<ItemRecord> findByUser(String userUuid) {
        log.info("ShopService.findByUser.in userUuid = {}", userUuid);
        var result = List.of(
            db.selectFrom(itemTable)
                .where(itemTable.ID.in(
                    db.select(orderTable.ITEM_ID)
                        .from(orderTable)
                        .where(orderTable.CUSTOMER.equal(userUuid)))
                )
                .fetchArray()
        );
        log.info("ShopService.findByUser.out result = {}", result);
        return result;
    }
}
