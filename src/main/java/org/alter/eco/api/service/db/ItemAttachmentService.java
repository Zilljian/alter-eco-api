package org.alter.eco.api.service.db;

import lombok.RequiredArgsConstructor;
import org.alter.eco.api.jooq.tables.ItemAttachment;
import org.alter.eco.api.jooq.tables.records.ItemAttachmentRecord;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ItemAttachmentService {

    private static final Logger log = LoggerFactory.getLogger(ItemAttachmentService.class);

    private final DSLContext db;

    private final ItemAttachment attachmentTable = ItemAttachment.ITEM_ATTACHMENT;

    public void attach(List<ItemAttachmentRecord> attachPhotos) {
        log.info("ItemAttachmentService.attach.in attachPhotos = {}", attachPhotos);
        db.batchInsert(attachPhotos)
            .execute();
        log.info("ItemAttachmentService.attach.out");
    }

    public void detach(Long itemId) {
        log.info("ItemAttachmentService.detach.in itemId = {}", itemId);
        db.deleteFrom(attachmentTable)
            .where(attachmentTable.ITEM_ID.equal(itemId))
            .execute();
        log.info("ItemAttachmentService.detach.out");
    }

    public Optional<ItemAttachmentRecord> findById(Long id) {
        log.info("ItemAttachmentService.findById.in id = {}", id);
        var result = db.selectFrom(attachmentTable)
            .where(attachmentTable.ID.equal(id))
            .fetchOptional();
        log.info("ItemAttachmentService.findById.out");
        return result;
    }

    public List<ItemAttachmentRecord> findIdsByItemId(Long itemId) {
        log.info("ItemAttachmentService.findIdsByItemId.in itemId = {}", itemId);
        var result = List.of(
            db.selectFrom(attachmentTable)
                .where(attachmentTable.ITEM_ID.equal(itemId))
                .fetchArray()
        );
        log.info("ItemAttachmentService.findIdsByItemId.out result = {}", result);
        return result;
    }

    public Map<Long, List<Long>> findIdsByItemIds(List<Long> itemIds) {
        log.info("ItemAttachmentService.findIdsByItemIds.in itemIds = {}", itemIds);
        var result = db.selectFrom(attachmentTable)
            .where(attachmentTable.ITEM_ID.in(itemIds))
            .fetchGroups(attachmentTable.ITEM_ID, attachmentTable.ID);
        log.info("ItemAttachmentService.findIdsByItemIds.out result = {}", result);
        return result;
    }
}
