package org.alter.eco.api.logic.task;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.alter.eco.api.jooq.enums.TaskStatus;
import org.alter.eco.api.jooq.tables.records.TaskRecord;
import org.alter.eco.api.logic.task.CreateTaskOperation.AttachPhotosRequest;
import org.alter.eco.api.service.db.AttachmentService;
import org.alter.eco.api.service.db.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.alter.eco.api.exception.ApplicationError.NOT_FOUND_BY_ID;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class EditTaskOperation {

    private final static Logger log = LoggerFactory.getLogger(EditTaskOperation.class);

    private final TaskService taskService;
    private final AttachmentService attachmentService;

    public void process(EditTaskRequest request) {
        log.info("EditTaskOperation.process.in request = {}", request);
        internalProcess(request);
        log.info("EditTaskOperation.process.out");
    }

    private void internalProcess(EditTaskRequest request) {
        var id = request.newTask.getId();
        var oldTask = taskService.findById(id)
            .orElseThrow(() -> NOT_FOUND_BY_ID.exception("No such task exist with id = " + id));
        var updated = updateRequest(request.newTask, oldTask);
        if (request.detach) {
            attachmentService.detach(id);
        }
        var attachmentRecords = request.attachPhotosRequest.stream()
            .map(AttachPhotosRequest::asRecord)
            .collect(toList());
        taskService.update(updated);
        attachmentService.attach(attachmentRecords);
    }

    private TaskRecord updateRequest(TaskRecord newTask, TaskRecord oldTask) {
        if (isNull(oldTask.getAssignee()) && nonNull(newTask.getAssignee())) {
            newTask.setStatus(TaskStatus.IN_PROGRESS);
            return newTask;
        } else if (nonNull(oldTask.getAssignee()) && isNull(newTask.getAssignee())) {
            newTask.setStatus(TaskStatus.TO_DO);
            return newTask;
        }
        return newTask;
    }

    @ToString(exclude = "attachPhotosRequest")
    @RequiredArgsConstructor
    public static class EditTaskRequest {

        public final TaskRecord newTask;
        public final List<AttachPhotosRequest> attachPhotosRequest;
        public final boolean detach;
    }
}
