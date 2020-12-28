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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RewardService {

    private static final Logger log = LoggerFactory.getLogger(RewardService.class);

    private final DSLContext db;

    private final Account accountTable = Account.ACCOUNT;
    private final Event eventTable = Event.EVENT;

    public Optional<AccountRecord> findByUser(String userUuid) {
        log.info("RewardService.findByUser.in userUuid = {}", userUuid);
        var result = db.insertInto(accountTable)
            .set(accountTable.USER_ID, userUuid)
            .set(accountTable.AMOUNT, 0L)
            .onConflictOnConstraint(accountTable.getPrimaryKey())
            .doNothing()
            .returning()
            .fetchOptional();
        log.info("RewardService.findByUser.out");
        return result;
    }

    public void insertEvent(EventRecord event) {
        log.info("RewardService.insertEvent.in event = {}", event);
        db.insertInto(eventTable)
            .set(event)
            .execute();
        log.info("RewardService.insertEvent.out");
    }

    public AccountRecord updateStatus(UpdateStatusRequest request) {
        log.info("RewardService.updateStatus.in request = {}", request);
        var result = db.update(accountTable)
            .set(accountTable.STATUS, request.status())
            .where(accountTable.USER_ID.equal(request.userUuid()))
            .returning(accountTable.USER_ID)
            .fetchOne();
        log.info("RewardService.updateStatus.out result = {}", result);
        return result;
    }

    public AccountRecord accrualAccount(AccrualRequest request) {
        log.info("RewardService.accrualAccount.in request = {}", request);
        var result = db.insertInto(accountTable)
            .set(accountTable.USER_ID, request.userUuid())
            .set(accountTable.AMOUNT, request.amount())
            .onConflictOnConstraint(accountTable.getPrimaryKey())
            .doUpdate()
            .set(accountTable.AMOUNT, accountTable.AMOUNT.add(request.amount()))
            .set(accountTable.UPDATED, LocalDateTime.now())
            .where(accountTable.USER_ID.equal(request.userUuid()))
            .and(accountTable.STATUS.equal(AccountStatus.ACTIVE))
            .returning(accountTable.USER_ID)
            .fetchOne();
        log.info("RewardService.accrualAccount.out result = {}", result);
        return result;
    }

    public AccountRecord writeoffAccount(WriteoffRequest request) {
        log.info("RewardService.writeoffAccount.in request = {}", request);
        var result = db.update(accountTable)
            .set(accountTable.AMOUNT, accountTable.AMOUNT.sub(request.amount()))
            .set(accountTable.UPDATED, LocalDateTime.now())
            .where(accountTable.USER_ID.equal(request.userUuid()))
            .and(accountTable.STATUS.equal(AccountStatus.ACTIVE))
            .returning(accountTable.USER_ID)
            .fetchOne();
        log.info("RewardService.writeoffAccount.out result = {}", result);
        return result;
    }
}
