package org.alter.eco.api.logic;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.alter.eco.api.jooq.enums.TaskStatus;
import org.alter.eco.api.jooq.tables.Task;
import org.alter.eco.api.jooq.tables.records.TaskRecord;
import org.alter.eco.api.model.Sort;
import org.alter.eco.api.service.db.AttachmentService;
import org.alter.eco.api.service.db.TaskService;
import org.jooq.Condition;
import org.jooq.SelectForUpdateStep;
import org.jooq.SelectWhereStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public class FindTasksOperation {

    private final static Logger log = LoggerFactory.getLogger(FindTasksOperation.class);

    private final TaskService taskService;
    private final AttachmentService attachmentService;

    public List<org.alter.eco.api.model.Task> process(FindTasksRequest request) {
        var tasks = taskService.findByFilters(request);
        var taskIds = tasks.stream().map(TaskRecord::getId).collect(toList());
        var attachments = attachmentService.findIdsByTaskId(taskIds);
        return tasks.stream()
            .map(t -> org.alter.eco.api.model.Task.of(t, attachments.get(t.getId())))
            .collect(toList());
    }

    public static record FindTasksRequest(
        @JsonProperty("status") TaskStatus status,
        @JsonProperty("assignee") String assignee,
        @JsonProperty("createdBy") String createdBy,
        @JsonProperty("searchString") String searchString,
        @JsonProperty("sort") List<Sort> sort,
        @JsonProperty("offset") Long offset,
        @JsonProperty("limit") Long limit) {

        @JsonCreator
        public FindTasksRequest {
        }

        public SelectForUpdateStep<TaskRecord> withCondition(SelectWhereStep<TaskRecord> query) {
            Condition condition = null;
            if (nonNull(searchString)) {
                condition = Task.TASK.TITLE.like(format("%s%%", searchString));
            }
            if (nonNull(status)) {
                condition = condition == null ?
                            Task.TASK.STATUS.equal(status) :
                            condition.and(Task.TASK.STATUS.equal(status));
            }
            if (nonNull(assignee)) {
                condition = condition == null ?
                            Task.TASK.ASSIGNEE.equal(assignee) :
                            condition.and(Task.TASK.ASSIGNEE.equal(assignee));
            }
            if (nonNull(createdBy)) {
                condition = condition == null ?
                            Task.TASK.CREATED_BY.equal(createdBy) :
                            condition.and(Task.TASK.CREATED_BY.equal(createdBy));
            }

            if (condition != null) {
                query.where(condition);
            }

            return query
                .orderBy(ofNullable(sort).orElse(List.of()).stream().map(Sort::value).collect(toList()))
                .limit(requireNonNullElse(limit, 20L))
                .offset(requireNonNullElse(offset, 0L));
        }
    }
}
