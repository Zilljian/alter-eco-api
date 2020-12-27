package org.alter.eco.api.logic.shop;

import lombok.RequiredArgsConstructor;
import org.alter.eco.api.jooq.tables.records.ItemAttachmentRecord;
import org.alter.eco.api.service.db.ItemAttachmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FindAttachmentsByItemIdOperation {

    private final static Logger log = LoggerFactory.getLogger(FindAttachmentsByItemIdOperation.class);

    private final ItemAttachmentService attachmentService;

    public List<ItemAttachmentRecord> process(Long taskId) {
        log.info("FindAttachmentsByItemIdOperation.process.in taskId = {}", taskId);
        var result = internalProcess(taskId);
        log.info("FindAttachmentsByItemIdOperation.process.out");
        return result;
    }

    private List<ItemAttachmentRecord> internalProcess(Long itemId) {
        var attachments = attachmentService.findIdsByItemId(itemId);
        if (attachments.isEmpty()) {
            log.warn("No attachments found for item id = " + itemId);
        }
        return attachments;
    }
}
