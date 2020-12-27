package org.alter.eco.api.service.db;

import lombok.RequiredArgsConstructor;
import org.alter.eco.api.jooq.tables.Attachment;
import org.alter.eco.api.jooq.tables.records.AttachmentRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AttachmentService {

    private final DSLContext db;

    private final Attachment attachmentTable = Attachment.ATTACHMENT;

    public void attach(List<AttachmentRecord> attachPhotos) {
        db.batchInsert(attachPhotos)
            .execute();
    }

    public void detach(Long taskId) {
        db.deleteFrom(attachmentTable)
            .where(attachmentTable.TASK_ID.equal(taskId))
            .execute();
    }

    public Optional<AttachmentRecord> findById(Long id) {
        return db.selectFrom(attachmentTable)
            .where(attachmentTable.ID.equal(id))
            .fetchOptional();
    }

    public List<AttachmentRecord> findIdsByTaskId(Long taskId) {
        return List.of(
            db.selectFrom(attachmentTable)
                .where(attachmentTable.TASK_ID.equal(taskId))
                .fetchArray()
        );
    }

    public Map<Long, List<Long>> findIdsByTaskId(List<Long> taskIds) {
        return db.selectFrom(attachmentTable)
            .where(attachmentTable.TASK_ID.in(taskIds))
            .fetchGroups(attachmentTable.TASK_ID, attachmentTable.ID);
    }
}
