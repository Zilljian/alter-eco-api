package org.alter.eco.api.service.db;

import lombok.RequiredArgsConstructor;
import org.alter.eco.api.jooq.tables.ItemAttachment;
import org.alter.eco.api.jooq.tables.records.ItemAttachmentRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ItemAttachmentService {

    private final DSLContext db;

    private final ItemAttachment attachmentTable = ItemAttachment.ITEM_ATTACHMENT;

    public void attach(List<ItemAttachmentRecord> attachPhotos) {
        db.batchInsert(attachPhotos)
            .execute();
    }

    public void detach(Long itemId) {
        db.deleteFrom(attachmentTable)
            .where(attachmentTable.ITEM_ID.equal(itemId))
            .execute();
    }

    public Optional<ItemAttachmentRecord> findById(Long id) {
        return db.selectFrom(attachmentTable)
            .where(attachmentTable.ID.equal(id))
            .fetchOptional();
    }

    public List<ItemAttachmentRecord> findIdsByItemId(Long itemId) {
        return List.of(
            db.selectFrom(attachmentTable)
                .where(attachmentTable.ITEM_ID.equal(itemId))
                .fetchArray()
        );
    }

    public Map<Long, List<Long>> findIdsByItemIds(List<Long> itemIds) {
        return db.selectFrom(attachmentTable)
            .where(attachmentTable.ITEM_ID.in(itemIds))
            .fetchGroups(attachmentTable.ITEM_ID, attachmentTable.ID);
    }
}
