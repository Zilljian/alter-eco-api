package org.alter.eco.api.logic.task;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.alter.eco.api.jooq.enums.TaskStatus;
import org.alter.eco.api.jooq.tables.records.AttachmentRecord;
import org.alter.eco.api.jooq.tables.records.TaskRecord;
import org.alter.eco.api.model.ChangingStatus;
import org.alter.eco.api.service.db.ApprovalService;
import org.alter.eco.api.service.db.AttachmentService;
import org.alter.eco.api.service.db.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toList;
import static org.alter.eco.api.exception.ValidationError.INVALID_ATTACH_REQUEST;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class CreateTaskOperation {

    private final static Logger log = LoggerFactory.getLogger(CreateTaskOperation.class);

    private final TaskService taskService;
    private final AttachmentService attachmentService;
    private final ApprovalService approvalService;

    public Long process(CreateTaskRequest request) {
        log.info("CreateTaskOperation.process.in id = {}", request);
        var result = internalProcess(request);
        log.info("CreateTaskOperation.process.out");
        return result;
    }

    private Long internalProcess(CreateTaskRequest request) {
        var newTask = request.newTask;
        newTask.setStatus(TaskStatus.WAITING_FOR_APPROVE);
        newTask.setCreatedBy(requireNonNullElse(MDC.get("user"), "default"));

        var taskId = taskService.insert(newTask).getId();
        var attachPhotos = request.attachPhotosRequest.stream()
            .peek(a -> a.setTaskId(taskId))
            .map(AttachPhotosRequest::validate)
            .map(AttachPhotosRequest::asRecord)
            .collect(toList());
        attachmentService.attach(attachPhotos);
        approvalService.insertOnConflictUpdate(ChangingStatus.waitingForApproveWith(taskId));
        return taskId;
    }

    @ToString(exclude = "attachPhotosRequest")
    @RequiredArgsConstructor
    public static class CreateTaskRequest {

        public final TaskRecord newTask;
        public final List<AttachPhotosRequest> attachPhotosRequest;
    }

    @Setter
    @ToString
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class AttachPhotosRequest {

        private Long taskId;
        public final String contentType;
        public final Long contentLength;
        public final byte[] content;

        public AttachPhotosRequest validate() {
            if (isNull(contentType)) {
                throw INVALID_ATTACH_REQUEST.exception("Content-Type cannot be null");
            } else if (isNull(content)) {
                throw INVALID_ATTACH_REQUEST.exception("Content cannot be null");
            }
            return this;
        }

        public AttachmentRecord asRecord() {
            var r = new AttachmentRecord();
            r.setTaskId(taskId);
            r.setType(contentType);
            r.setContent(content);
            r.setLength(contentLength);
            return r;
        }
    }
}
