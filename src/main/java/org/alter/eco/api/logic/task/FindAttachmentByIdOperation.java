package org.alter.eco.api.logic.task;

import lombok.RequiredArgsConstructor;
import org.alter.eco.api.service.db.AttachmentService;
import org.alter.eco.api.jooq.tables.records.AttachmentRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.alter.eco.api.exception.ApplicationError.ATTACHMENTS_NOT_FOUND;

@Component
@RequiredArgsConstructor
public class FindAttachmentByIdOperation {

    private final static Logger log = LoggerFactory.getLogger(FindAttachmentByIdOperation.class);

    private final AttachmentService attachmentService;

    public AttachmentRecord process(Long id) {
        log.info("FindAttachmentByIdOperation.process.in id = {}", id);
        var result = internalProcess(id);
        log.info("FindAttachmentByIdOperation.process.out");
        return result;
    }

    private AttachmentRecord internalProcess(Long id) {
        return attachmentService.findById(id)
            .orElseThrow(() -> ATTACHMENTS_NOT_FOUND.exception("No attachments found with id = " + id));
    }
}
