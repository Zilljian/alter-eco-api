package org.alter.eco.api.service.db;

import lombok.RequiredArgsConstructor;
import org.alter.eco.api.jooq.tables.Task;
import org.alter.eco.api.jooq.tables.records.TaskRecord;
import org.alter.eco.api.logic.task.FindTasksOperation.FindTasksRequest;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final DSLContext db;

    private final Task taskTable = Task.TASK;

    public TaskRecord insert(TaskRecord task) {
        log.info("TaskService.insert.in task = {}", task);
        var result = db.insertInto(taskTable)
            .set(task)
            .returning(taskTable.ID)
            .fetchOne();
        log.info("TaskService.insert.out result = {}", result);
        return result;
    }

    public Optional<TaskRecord> findById(Long id) {
        log.info("TaskService.findById.in id = {}", id);
        var result = db.selectFrom(taskTable)
            .where(taskTable.ID.equal(id))
            .fetchOptional();
        log.info("TaskService.findById.out result = {}", result);
        return result;
    }

    public void update(TaskRecord forUpdate) {
        log.info("TaskService.update.in forUpdate = {}", forUpdate);
        forUpdate.setUpdated(LocalDateTime.now());
        db.update(taskTable)
            .set(forUpdate)
            .where(taskTable.ID.equal(forUpdate.getId()))
            .execute();
        log.info("TaskService.update.out");
    }

    public List<TaskRecord> findByFilters(FindTasksRequest request) {
        log.info("TaskService.findByFilters.in request = {}", request);
        var result = List.of(
            request.withCondition(db.selectFrom(taskTable))
                .fetchArray()
        );
        log.info("TaskService.findByFilters.out result = {}", result);
        return result;
    }
}
