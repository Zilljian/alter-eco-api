package org.alter.eco.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.alter.eco.api.jooq.enums.TaskStatus;
import org.alter.eco.api.jooq.enums.TaskType;
import org.alter.eco.api.jooq.tables.records.TaskRecord;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Optional.ofNullable;

public record Task(
    @JsonProperty("id") Long id,
    @JsonProperty("title") String title,
    @JsonProperty("description") String description,
    @JsonProperty("status") TaskStatus status,
    @JsonProperty("coordinate") Point coordinate,
    @JsonProperty("type") TaskType type,
    @JsonProperty("reward") Long reward,
    @JsonProperty("attachmentIds") List<Long> attachmentIds,
    @JsonProperty("assignee") String assignee,
    @JsonProperty("dueDate") LocalDateTime dueDate,
    @JsonProperty("updated") LocalDateTime updated,
    @JsonProperty("createdBy") String createdBy,
    @JsonProperty("created") LocalDateTime created) {

    @JsonCreator
    public Task {
    }

    public TaskRecord toRecord() {
        var t = new TaskRecord();
        ofNullable(id).ifPresent(t::setId);
        ofNullable(title).ifPresent(t::setTitle);
        ofNullable(description).ifPresent(t::setDescription);
        ofNullable(status).ifPresent(t::setStatus);
        ofNullable(coordinate).ifPresent(t::setCoordinate);
        ofNullable(reward).ifPresent(t::setReward);
        ofNullable(assignee).ifPresent(t::setAssignee);
        ofNullable(type).ifPresent(t::setType);
        ofNullable(dueDate).ifPresent(t::setDueDate);
        ofNullable(updated).ifPresent(t::setUpdated);
        ofNullable(createdBy).ifPresent(t::setCreatedBy);
        ofNullable(created).ifPresent(t::setCreated);
        return t;
    }

    public static Task of(TaskRecord taskRecord, List<Long> attachmentIds) {
        return new Task(
            taskRecord.getId(),
            taskRecord.getTitle(),
            taskRecord.getDescription(),
            taskRecord.getStatus(),
            taskRecord.getCoordinate(),
            taskRecord.getType(),
            taskRecord.getReward(),
            attachmentIds,
            taskRecord.getAssignee(),
            taskRecord.getDueDate(),
            taskRecord.getUpdated(),
            taskRecord.getCreatedBy(),
            taskRecord.getCreated()
        );
    }
}
