package org.alter.eco.api.service.db;

import lombok.RequiredArgsConstructor;
import org.alter.eco.api.jooq.tables.Task;
import org.alter.eco.api.jooq.tables.records.TaskRecord;
import org.alter.eco.api.logic.FindTasksOperation.FindTasksRequest;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TaskService {

    private final DSLContext db;

    private final Task taskTable = Task.TASK;

    public Long insert(TaskRecord task) {
        return db.insertInto(taskTable)
            .set(task)
            .returning(taskTable.ID)
            .fetchOne()
            .getId();
    }

    public Optional<TaskRecord> findById(Long id) {
        return db.selectFrom(taskTable)
            .where(taskTable.ID.equal(id))
            .fetchOptional();
    }

    public void update(TaskRecord forUpdate) {
        forUpdate.setUpdated(LocalDateTime.now());
        db.update(taskTable)
            .set(forUpdate)
            .where(taskTable.ID.equal(forUpdate.getId()))
            .execute();
    }

    public List<TaskRecord> findByFilters(FindTasksRequest request) {
        return List.of(
            request.withCondition(db.selectFrom(taskTable))
                .fetchArray()
        );
    }
}
