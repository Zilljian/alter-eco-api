package org.alter.eco.api.logic.task;

import lombok.RequiredArgsConstructor;
import org.alter.eco.api.jooq.tables.records.AttachmentRecord;
import org.alter.eco.api.service.db.AttachmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.alter.eco.api.exception.ApplicationError.ATTACHMENTS_NOT_FOUND;

@Component
@RequiredArgsConstructor
public class FindAttachmentsByTaskIdOperation {

    private final static Logger log = LoggerFactory.getLogger(FindAttachmentsByTaskIdOperation.class);

    private final AttachmentService attachmentService;

    public List<AttachmentRecord> process(Long taskId) {
        log.info("FindAttachmentsByTaskIdOperation.process.in taskId = {}", taskId);
        var result = internalProcess(taskId);
        log.info("FindAttachmentsByTaskIdOperation.process.out");
        return result;
    }

    private List<AttachmentRecord> internalProcess(Long taskId) {
        var attachments = attachmentService.findIdsByTaskId(taskId);
        if (attachments.isEmpty()) {
            throw ATTACHMENTS_NOT_FOUND.exception("No attachments found for task id = " + taskId);
        }
        return attachments;
    }
}
