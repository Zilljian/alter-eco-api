package org.alter.eco.api.logic.task;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.alter.eco.api.jooq.enums.TaskStatus;
import org.alter.eco.api.model.ChangingStatus;
import org.alter.eco.api.service.db.ApprovalService;
import org.alter.eco.api.service.db.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static java.lang.String.format;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class UpdateTaskStatusOperation {

    private final static Logger log = LoggerFactory.getLogger(UpdateTaskStatusOperation.class);

    public final TaskService taskService;
    private final ApprovalService approvalService;

    public void process(UpdateStatusRequest request) {
        log.info("UpdateTaskStatusOperation.process.in request = {}", request);
        internalProcess(request);
        log.info("UpdateTaskStatusOperation.process.out");
    }

    private void internalProcess(UpdateStatusRequest request) {
        var taskId = request.taskId;
        var task = taskService.findById(taskId)
            .orElseThrow(() -> new RuntimeException("No such task exist with id = " + taskId));
        var oldTaskStatus = task
            .getStatus();
        var newStatus = request.status;
        if (newStatus == TaskStatus.APPROVED) {
            newStatus = TaskStatus.TO_DO;
        }
        task.setStatus(newStatus);
        validateOver(oldTaskStatus, newStatus);
        taskService.update(task);
        if (newStatus == TaskStatus.RESOLVED) {
            approvalService.insertOnConflictUpdate(ChangingStatus.resolvedWith(taskId));
        }
    }

    @ToString
    @RequiredArgsConstructor
    public static class UpdateStatusRequest {

        public final Long taskId;
        public final TaskStatus status;
    }

    public void validateOver(TaskStatus oldStatus, TaskStatus newStatus) {
        if (oldStatus.compareTo(newStatus) > 0) {
            throw new IllegalStateException(format("It's not available to change status from %s to %s", oldStatus, newStatus));
        }
    }
}
