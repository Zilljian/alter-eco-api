package org.alter.eco.api.model;

import org.alter.eco.api.jooq.enums.TaskStatus;

public record ChangingStatus(Long taskId, TaskStatus status) {

    public static ChangingStatus resolvedWith(Long taskId) {
        return new ChangingStatus(taskId, TaskStatus.RESOLVED);
    }

    public static ChangingStatus waitingForApproveWith(Long taskId) {
        return new ChangingStatus(taskId, TaskStatus.WAITING_FOR_APPROVE);
    }
}
