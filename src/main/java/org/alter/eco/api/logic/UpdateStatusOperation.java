package org.alter.eco.api.logic;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.alter.eco.api.jooq.enums.TaskStatus;
import org.alter.eco.api.service.db.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Component
@RequiredArgsConstructor
public class UpdateStatusOperation {

    private final static Logger log = LoggerFactory.getLogger(UpdateStatusOperation.class);

    public final TaskService taskService;

    public void process(UpdateStatusRequest request) {
        var task = taskService.findById(request.taskId)
            .orElseThrow(() -> new RuntimeException("No such task exist with id = " + request.taskId));
        var oldTaskStatus = task
            .getStatus();
        var newStatus = request.status;
        task.setStatus(newStatus);
        validateOver(oldTaskStatus, newStatus);
        taskService.update(task);
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
