package org.alter.eco.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.alter.eco.api.jooq.enums.AccountStatus;
import org.alter.eco.api.jooq.tables.records.AccountRecord;

import java.time.LocalDateTime;

public record Account(
    @JsonProperty("userId") String userId,
    @JsonProperty("amount") Long amount,
    @JsonProperty("status") AccountStatus status,
    @JsonProperty("updated") LocalDateTime updated,
    @JsonProperty("created") LocalDateTime created) {

    @JsonCreator
    public Account {
    }

    public static Account fromRecord(AccountRecord record) {
        return new Account(
            record.getUserId(),
            record.getAmount(),
            record.getStatus(),
            record.getUpdated().toLocalDateTime(),
            record.getCreated().toLocalDateTime()
        );
    }
}
