package org.alter.eco.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.alter.eco.api.jooq.enums.TaskStatus;
import org.alter.eco.api.logic.task.CreateTaskOperation;
import org.alter.eco.api.logic.task.CreateTaskOperation.AttachPhotosRequest;
import org.alter.eco.api.logic.task.CreateTaskOperation.CreateTaskRequest;
import org.alter.eco.api.logic.task.EditTaskOperation;
import org.alter.eco.api.logic.task.EditTaskOperation.EditTaskRequest;
import org.alter.eco.api.logic.task.FindAttachmentByIdOperation;
import org.alter.eco.api.logic.task.FindAttachmentsByTaskIdOperation;
import org.alter.eco.api.logic.task.FindTaskByIdOperation;
import org.alter.eco.api.logic.task.FindTasksOperation;
import org.alter.eco.api.logic.task.FindTasksOperation.FindTasksRequest;
import org.alter.eco.api.logic.task.UpdateTaskStatusOperation;
import org.alter.eco.api.logic.task.UpdateTaskStatusOperation.UpdateStatusRequest;
import org.alter.eco.api.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import javax.validation.Valid;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {

    private final static Logger log = LoggerFactory.getLogger(TaskController.class);

    private final ControllerHelper helper;

    private final CreateTaskOperation createTaskOperation;
    private final FindTasksOperation findTasksOperation;
    private final FindTaskByIdOperation findTaskByIdOperation;
    private final FindAttachmentByIdOperation findAttachmentByIdOperation;
    private final FindAttachmentsByTaskIdOperation findAttachmentsByTaskIdOperation;
    private final EditTaskOperation editTaskOperation;
    private final UpdateTaskStatusOperation updateTaskStatusOperation;

    @PostMapping("/tasks")
    @Operation(
        description = "Find all tasks that satisfy the conditions.",
        summary = "Find tasks by filters"
    )
    public List<Task> findTasks(
        @Parameter(description = "'searchString' allows fulltext search in title field.")
        @Valid @RequestBody FindTasksRequest request,
        @RequestHeader("Authorization") String token) {
        log.info("TaskController.findTasks.in request = {}", request);
        helper.obtainToken(token);
        var result = findTasksOperation.process(request);
        log.info("TaskController.findTasks.out");
        return result;
    }

    @GetMapping("/task/{id}")
    @Operation(
        summary = "Get task by id"
    )
    public Task getTaskById(@PathVariable(value = "id") Long id,
                            @RequestHeader("Authorization") String token) {
        log.info("TaskController.getTaskById.in id = {}", id);
        helper.obtainToken(token);
        var result = findTaskByIdOperation.process(id);
        log.info("TaskController.getTaskById.out");
        return result;
    }

    @PostMapping(value = "/task",
        consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE
        })
    @Operation(
        description = "Create task with attachments. If task has been created it doesn't mean that it will be approved.",
        summary = "Create task"
    )
    public Long createTask(@RequestPart("task") Task task,
                           @RequestPart(value = "attachment", required = false) List<MultipartFile> attachment,
                           @RequestHeader("Authorization") String token) {
        log.info("TaskController.createTask.in task = {}", task);
        helper.obtainToken(token);
        var attachments = ofNullable(attachment).orElse(List.of()).stream()
            .map(a -> {
                try {
                    return new AttachPhotosRequest(a.getContentType(), a.getSize(), a.getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).collect(toList());
        var request = new CreateTaskRequest(task.toRecord(), attachments);
        var result = createTaskOperation.process(request);
        log.info("TaskController.createTask.out");
        return result;
    }

    @PutMapping(value = "/task", params = "detach")
    @Operation(
        description = "Edit existed task. Allowed only for user created this task.",
        summary = "Edit task"
    )
    public void editTask(@RequestPart("task") Task task,
                         @RequestPart(value = "attachment", required = false) List<MultipartFile> attachment,
                         @Parameter(description = "Whether delete or not delete attached photos.")
                         @RequestParam(value = "detach") boolean detach,
                         @RequestHeader("Authorization") String token) {
        log.info("TaskController.editTask.in task = {}", task);
        helper.obtainToken(token);
        var attachments = ofNullable(attachment).orElse(List.of()).stream()
            .map(a -> {
                try {
                    return new AttachPhotosRequest(task.id(), a.getContentType(), a.getSize(), a.getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).collect(toList());
        var request = new EditTaskRequest(task.toRecord(), attachments, detach);
        editTaskOperation.process(request);
        log.info("TaskController.editTask.out");
    }

    @PatchMapping("/task/{id}")
    @Operation(
        description = "Update task status. It's useful only for turning task to RESOLVED stage. Another statuses are processed internally.",
        summary = "Update task status"
    )
    public void updateStatus(@PathVariable(value = "id") Long id,
                             @RequestParam("status") TaskStatus status,
                             @RequestHeader("Authorization") String token) {
        log.info("TaskController.updateStatus.in id = {}, status = {}", id, status);
        helper.obtainToken(token);
        var request = new UpdateStatusRequest(id, status);
        updateTaskStatusOperation.process(request);
        log.info("TaskController.updateStatus.out");
    }

    @GetMapping(value = "/task/{id}/attachment", produces = {
        MediaType.MULTIPART_FORM_DATA_VALUE
    })
    @ResponseBody
    @Operation(
        description = "Get all task's attachments by task's id.",
        summary = "Get attachments by task id"
    )
    public MultiValueMap<String, HttpEntity<?>> findAttachmentsByTaskId(
        @Parameter(description = "Task's id.")
        @PathVariable(value = "id") Long taskId,
        @RequestHeader("Authorization") String token) {
        log.info("TaskController.findAttachments.in id = {}", taskId);
        helper.obtainToken(token);
        var result = findAttachmentsByTaskIdOperation.process(taskId);
        var builder = new MultipartBodyBuilder();
        result.forEach(a -> builder.part(
            "attachment",
            a.getContent(),
            MediaType.valueOf(a.getType())
        ));
        var response = builder.build();
        log.info("TaskController.findAttachments.out");
        return response;
    }

    @GetMapping(value = "/attachment/{id}",
        produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE})
    @ResponseBody
    @Operation(
        description = "Get certain task's attachment by it's id.",
        summary = "Get attachment by id"
    )
    public byte[] findAttachmentsById(
        @Parameter(description = "Attachment's id.")
        @PathVariable(value = "id") Long id,
        @RequestHeader("Authorization") String token) {
        log.info("TaskController.findAttachments.in id = {}", id);
        helper.obtainToken(token);
        var result = findAttachmentByIdOperation.process(id).getContent();
        log.info("TaskController.findAttachments.out");
        return result;
    }
}
