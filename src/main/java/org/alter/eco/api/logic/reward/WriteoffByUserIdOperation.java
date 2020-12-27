package org.alter.eco.api.logic.reward;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.alter.eco.api.jooq.enums.AccountStatus;
import org.alter.eco.api.jooq.tables.records.EventRecord;
import org.alter.eco.api.service.db.RewardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static java.lang.String.format;
import static org.alter.eco.api.exception.ApplicationError.INTERNAL_ERROR;
import static org.alter.eco.api.exception.ApplicationError.NOT_ENOUGH_MONEY;
import static org.alter.eco.api.exception.ApplicationError.WRONG_STATUS;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class WriteoffByUserIdOperation {

    private final static Logger log = LoggerFactory.getLogger(WriteoffByUserIdOperation.class);

    private final RewardService rewardService;

    public void process(WriteoffRequest request) {
        log.info("WriteoffByClientIdOperation.process.in id = {}", request);
        try {
            internalProcess(request);
        } catch (Exception e) {
            log.warn("WriteoffByClientIdOperation.process.thrown", e);
            throw INTERNAL_ERROR.exception(e);
        }
        log.info("WriteoffByClientIdOperation.process.out");
    }

    private void internalProcess(WriteoffRequest request) {
        rewardService.findByUser(request.userUuid()).ifPresent(a -> {
            if (a.getAmount() < request.amount) {
                throw NOT_ENOUGH_MONEY.exception(format("Cannot write off %s. Not enough money", request.amount));
            } else if (!a.getStatus().equals(AccountStatus.ACTIVE)) {
                throw WRONG_STATUS.exception(format("Account status is %s. Cannot write off from this account", a.getStatus()));
            }
            var updatedAccount = rewardService.writeoffAccount(request);

            sendEvent(updatedAccount.getId(), request.amount(), request.initiator());
        });
    }

    private void sendEvent(Long accountId, Long value, String initiator) {
        var event = new EventRecord();
        event.setAccountId(accountId);
        event.setValue(value);
        event.setName("WriteoffAccount");
        event.setInitiator(initiator);
        rewardService.insertEvent(event);
    }

    @ToString(exclude = "amount")
    public static record WriteoffRequest(String userUuid, Long amount, String initiator) {

        public static WriteoffRequest system(String userUuid, Long amount) {
            return new WriteoffRequest(userUuid, amount, "system");
        }
    }
}
