package org.alter.eco.api.logic.task;

import lombok.RequiredArgsConstructor;
import org.alter.eco.api.jooq.tables.records.AttachmentRecord;
import org.alter.eco.api.model.Task;
import org.alter.eco.api.service.db.AttachmentService;
import org.alter.eco.api.service.db.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.toList;
import static org.alter.eco.api.exception.ApplicationError.TASK_NOT_FOUND_BY_ID;

@Component
@RequiredArgsConstructor
public class FindTaskByIdOperation {

    private final static Logger log = LoggerFactory.getLogger(FindTaskByIdOperation.class);

    private final TaskService taskService;
    private final AttachmentService attachmentService;

    public Task process(Long id) {
        log.info("FindTaskByIdOperation.process.in id = {}", id);
        var result = internalProcess(id);
        log.info("FindTaskByIdOperation.process.out");
        return result;
    }

    private Task internalProcess(Long id) {
        var task = taskService.findById(id)
            .orElseThrow(() -> TASK_NOT_FOUND_BY_ID.exception("No such task exist with id = " + id));
        var attachments = attachmentService.findIdsByTaskId(task.getId()).stream()
            .map(AttachmentRecord::getId)
            .collect(toList());
        return Task.of(task, attachments);
    }
}
