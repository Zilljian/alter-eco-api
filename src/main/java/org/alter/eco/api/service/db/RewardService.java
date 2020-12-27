package org.alter.eco.api.service.db;

import lombok.RequiredArgsConstructor;
import org.alter.eco.api.jooq.enums.AccountStatus;
import org.alter.eco.api.jooq.tables.Account;
import org.alter.eco.api.jooq.tables.Event;
import org.alter.eco.api.jooq.tables.records.AccountRecord;
import org.alter.eco.api.jooq.tables.records.EventRecord;
import org.alter.eco.api.logic.reward.AccrualByClientIdOperation.AccrualRequest;
import org.alter.eco.api.logic.reward.UpdateAccountStatusOperation.UpdateStatusRequest;
import org.alter.eco.api.logic.reward.WriteoffByUserIdOperation.WriteoffRequest;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RewardService {

    private final DSLContext db;

    private final Account accountTable = Account.ACCOUNT;
    private final Event eventTable = Event.EVENT;

    public Optional<AccountRecord> findByUser(String userUuid) {
        return db.insertInto(accountTable)
            .set(accountTable.USER_ID, userUuid)
            .set(accountTable.AMOUNT, 0L)
            .onConflictOnConstraint(accountTable.getPrimaryKey())
            .doNothing()
            .returning()
            .fetchOptional();
    }

    public void insertEvent(EventRecord event) {
        db.insertInto(eventTable)
            .set(event)
            .execute();
    }

    public AccountRecord updateStatus(UpdateStatusRequest request) {
        return db.update(accountTable)
            .set(accountTable.STATUS, request.status())
            .where(accountTable.USER_ID.equal(request.userUuid()))
            .returning(accountTable.ID)
            .fetchOne();
    }

    public AccountRecord accrualAccount(AccrualRequest request) {
        return db.insertInto(accountTable)
            .set(accountTable.USER_ID, request.userUuid())
            .set(accountTable.AMOUNT, request.amount())
            .onConflictOnConstraint(accountTable.getPrimaryKey())
            .doUpdate()
            .set(accountTable.AMOUNT, accountTable.AMOUNT.add(request.amount()))
            .set(accountTable.UPDATED, LocalDateTime.now())
            .where(accountTable.USER_ID.equal(request.userUuid()))
            .and(accountTable.STATUS.equal(AccountStatus.ACTIVE))
            .returning(accountTable.ID)
            .fetchOne();
    }

    public AccountRecord writeoffAccount(WriteoffRequest request) {
        return db.insertInto(accountTable)
            .set(accountTable.USER_ID, request.userUuid())
            .set(accountTable.AMOUNT, request.amount())
            .onConflictOnConstraint(accountTable.getPrimaryKey())
            .doUpdate()
            .set(accountTable.AMOUNT, accountTable.AMOUNT.sub(request.amount()))
            .set(accountTable.UPDATED, LocalDateTime.now())
            .where(accountTable.USER_ID.equal(request.userUuid()))
            .and(accountTable.STATUS.equal(AccountStatus.ACTIVE))
            .returning(accountTable.ID)
            .fetchOne();
    }
}
