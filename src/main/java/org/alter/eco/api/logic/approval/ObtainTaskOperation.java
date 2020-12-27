package org.alter.eco.api.logic.approval;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.alter.eco.api.jooq.enums.TaskStatus;
import org.alter.eco.api.logic.task.UpdateTaskStatusOperation;
import org.alter.eco.api.logic.task.UpdateTaskStatusOperation.UpdateStatusRequest;
import org.alter.eco.api.service.db.ApprovalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor
public class ObtainTaskOperation {

    private final static Logger log = LoggerFactory.getLogger(ObtainTaskOperation.class);

    private final ApprovalService approvalService;
    private final UpdateTaskStatusOperation updateTaskStatusOperation;

    public void process(ObtainTaskRequest request) {
        log.info("ObtainTaskOperation.process.in id = {}", request);
        internalProcess(request);
        log.info("ObtainTaskOperation.process.out");
    }

    private void internalProcess(ObtainTaskRequest request) {
        resolveInsert(request.status)
            .ifPresent(s -> approvalService.insertOnConflictUpdate(new ObtainTaskRequest(request.taskId, s)));
        resolveUpdateStatusRequest(request.status, request.taskId)
            .ifPresent(updateTaskStatusOperation::process);
    }

    private Optional<UpdateStatusRequest> resolveUpdateStatusRequest(TaskStatus status, Long taskId) {
        return ofNullable(switch (status) {
            case CREATED -> new UpdateStatusRequest(taskId, TaskStatus.WAITING_FOR_APPROVE);
            case APPROVED -> new UpdateStatusRequest(taskId, TaskStatus.TO_DO);
            default -> null;
        });
    }

    private Optional<TaskStatus> resolveInsert(TaskStatus status) {
        return ofNullable(switch (status) {
            case CREATED -> TaskStatus.WAITING_FOR_APPROVE;
            case RESOLVED -> TaskStatus.RESOLVED;
            default -> null;
        });
    }

    @ToString
    @RequiredArgsConstructor
    public static class ObtainTaskRequest {

        public final Long taskId;
        public final TaskStatus status;
    }
}
