package org.alter.eco.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.alter.eco.api.jooq.enums.ItemType;
import org.alter.eco.api.jooq.tables.records.ItemRecord;

import java.util.List;

import static java.util.Optional.ofNullable;

public record Item(
    @JsonProperty("id") Long id,
    @JsonProperty("title") String title,
    @JsonProperty("description") String description,
    @JsonProperty("type") ItemType type,
    @JsonProperty("price") Long price,
    @JsonProperty("attachmentIds") List<Long> attachmentIds,
    @JsonProperty("amount") Long amount,
    @JsonProperty("createdBy") String createdBy) {

    @JsonCreator
    public Item {
    }

    public ItemRecord toRecord() {
        var t = new ItemRecord();
        ofNullable(id).ifPresent(t::setId);
        ofNullable(title).ifPresent(t::setTitle);
        ofNullable(description).ifPresent(t::setDescription);
        ofNullable(price).ifPresent(t::setPrice);
        ofNullable(type).ifPresent(t::setType);
        ofNullable(amount).ifPresent(t::setAmount);
        ofNullable(createdBy).ifPresent(t::setCreatedBy);
        return t;
    }

    public static Item of(ItemRecord taskRecord, List<Long> attachmentIds) {
        return new Item(
            taskRecord.getId(),
            taskRecord.getTitle(),
            taskRecord.getDescription(),
            taskRecord.getType(),
            taskRecord.getPrice(),
            attachmentIds,
            taskRecord.getAmount(),
            taskRecord.getCreatedBy()
        );
    }

    public static Item of(ItemRecord taskRecord) {
        return new Item(
            taskRecord.getId(),
            taskRecord.getTitle(),
            taskRecord.getDescription(),
            taskRecord.getType(),
            taskRecord.getPrice(),
            null,
            taskRecord.getAmount(),
            taskRecord.getCreatedBy()
        );
    }
}
