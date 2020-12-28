package org.alter.eco.api.service.db;

import lombok.RequiredArgsConstructor;
import org.alter.eco.api.jooq.tables.Attachment;
import org.alter.eco.api.jooq.tables.records.AttachmentRecord;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AttachmentService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentService.class);

    private final DSLContext db;

    private final Attachment attachmentTable = Attachment.ATTACHMENT;

    public void attach(List<AttachmentRecord> attachPhotos) {
        log.info("AttachmentService.attach.in attachPhotos = {}", attachPhotos);
        db.batchInsert(attachPhotos)
            .execute();
        log.info("AttachmentService.attach.out");
    }

    public void detach(Long taskId) {
        log.info("AttachmentService.detach.in taskId = {}", taskId);
        db.deleteFrom(attachmentTable)
            .where(attachmentTable.TASK_ID.equal(taskId))
            .execute();
        log.info("AttachmentService.detach.out");
    }

    public Optional<AttachmentRecord> findById(Long id) {
        log.info("AttachmentService.findById.in id = {}", id);
        var result = db.selectFrom(attachmentTable)
            .where(attachmentTable.ID.equal(id))
            .fetchOptional();
        log.info("AttachmentService.findById.out");
        return result;
    }

    public List<AttachmentRecord> findIdsByTaskId(Long taskId) {
        log.info("AttachmentService.findIdsByTaskId.in taskId = {}", taskId);
        var result = List.of(
            db.selectFrom(attachmentTable)
                .where(attachmentTable.TASK_ID.equal(taskId))
                .fetchArray()
        );
        log.info("AttachmentService.findIdsByTaskId.out result = {}", result);
        return result;
    }

    public Map<Long, List<Long>> findIdsByTaskId(List<Long> taskIds) {
        log.info("AttachmentService.findIdsByTaskId.in taskIds = {}", taskIds);
        var result = db.selectFrom(attachmentTable)
            .where(attachmentTable.TASK_ID.in(taskIds))
            .fetchGroups(attachmentTable.TASK_ID, attachmentTable.ID);
        log.info("AttachmentService.findIdsByTaskId.out result = {}", result);
        return result;
    }
}
