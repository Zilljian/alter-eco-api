package org.alter.eco.api.service.db;

import lombok.RequiredArgsConstructor;
import org.alter.eco.api.jooq.tables.Item;
import org.alter.eco.api.jooq.tables.Order;
import org.alter.eco.api.jooq.tables.records.ItemRecord;
import org.alter.eco.api.logic.shop.FindItemsOperation.FindItemsRequest;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ShopService {

    private final DSLContext db;

    private final Item itemTable = Item.ITEM;
    private final Order orderTable = Order.ORDER;

    public ItemRecord insert(ItemRecord item) {
        return db.insertInto(itemTable)
            .set(item)
            .returning(itemTable.ID)
            .fetchOne();
    }

    public Optional<ItemRecord> findById(Long id) {
        return db.selectFrom(itemTable)
            .where(itemTable.ID.equal(id))
            .fetchOptional();
    }

    public void update(ItemRecord forUpdate) {
        db.update(itemTable)
            .set(forUpdate)
            .where(itemTable.ID.equal(forUpdate.getId()))
            .execute();
    }

    public List<ItemRecord> findByFilters(FindItemsRequest request) {
        return List.of(
            request.withCondition(db.selectFrom(itemTable))
                .fetchArray()
        );
    }

    public void purchaseItem(String userUuid, Long itemId) {
        db.insertInto(orderTable)
            .set(orderTable.CUSTOMER, userUuid)
            .set(orderTable.ITEM_ID, itemId)
            .execute();
    }

    public List<ItemRecord> findByUser(String userUuid) {
        return List.of(
            db.selectFrom(itemTable)
                .where(itemTable.ID.in(
                    db.select(orderTable.ITEM_ID)
                        .from(orderTable)
                        .where(orderTable.CUSTOMER.equal(userUuid)))
                )
                .fetchArray()
        );
    }
}
