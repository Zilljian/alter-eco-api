package org.alter.eco.api.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.alter.eco.api.jooq.enums.AccountStatus;
import org.alter.eco.api.jooq.tables.records.AccountRecord;

import java.time.LocalDateTime;

@Builder
@ToString
@EqualsAndHashCode(exclude = {"updated", "created"})
@RequiredArgsConstructor
public class Account {

    public final Long id;
    public final String userId;
    public final Long amount;
    public final AccountStatus status;
    public final LocalDateTime updated;
    public final LocalDateTime created;

    public static Account fromRecord(AccountRecord record) {
        return Account.builder()
            .userId(record.getUserId())
            .amount(record.getAmount())
            .status(record.getStatus())
            .updated(record.getUpdated())
            .created(record.getCreated())
            .build();
    }
}
