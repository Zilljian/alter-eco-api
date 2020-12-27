package org.alter.eco.api.logic.shop;

import lombok.RequiredArgsConstructor;
import org.alter.eco.api.jooq.tables.records.ItemAttachmentRecord;
import org.alter.eco.api.service.db.ItemAttachmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static org.alter.eco.api.exception.ApplicationError.ATTACHMENTS_NOT_FOUND;

@Component
@RequiredArgsConstructor
public class FindItemAttachmentByIdOperation {

    private final static Logger log = LoggerFactory.getLogger(FindItemAttachmentByIdOperation.class);

    private final ItemAttachmentService attachmentService;

    public ItemAttachmentRecord process(Long id) {
        log.info("FindItemAttachmentByIdOperation.process.in id = {}", id);
        var result = internalProcess(id);
        log.info("FindItemAttachmentByIdOperation.process.out");
        return result;
    }

    private ItemAttachmentRecord internalProcess(Long id) {
        return attachmentService.findById(id)
            .orElseThrow(() -> ATTACHMENTS_NOT_FOUND.exception("No attachments found with id = " + id));
    }
}
