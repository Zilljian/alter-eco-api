package org.alter.eco.api.logic.reward;

import lombok.RequiredArgsConstructor;
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
import static org.alter.eco.api.exception.ApplicationError.WRONG_STATUS;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class AccrualByClientIdOperation {

    private final static Logger log = LoggerFactory.getLogger(AccrualByClientIdOperation.class);

    private final RewardService rewardService;

    public void process(AccrualRequest request) {
        log.info("AccrualByClientIdOperation.process.in id = {}", request);
        try {
            internalProcess(request);
        } catch (Exception e) {
            log.warn("AccrualByClientIdOperation.process.thrown", e);
            throw INTERNAL_ERROR.exception(e);
        }
        log.info("AccrualByClientIdOperation.process.out");
    }

    private void internalProcess(AccrualRequest request) {
        var account = rewardService.findByUser(request.userUuid());
        account.ifPresentOrElse(a -> {
            if (!a.getStatus().equals(AccountStatus.ACTIVE)) {
                throw WRONG_STATUS.exception(format("Account status is %s. Cannot accrual this account", a.getStatus()));
            }
            rewardService.accrualAccount(request);
            sendEvent(a.getId(), request.amount(), request.initiator());
        }, () -> {
            var updatedAccount = rewardService.accrualAccount(request);
            sendEvent(updatedAccount.getId(), request.amount(), request.initiator());
        });
    }

    private void sendEvent(Long accountId, Long value, String initiator) {
        var event = new EventRecord();
        event.setAccountId(accountId);
        event.setValue(value);
        event.setName("AccuralAccount");
        event.setInitiator(initiator);
        rewardService.insertEvent(event);
    }

    public static record AccrualRequest(String userUuid, Long amount, String initiator) {

        public static AccrualRequest system(String userUuid, Long amount) {
            return new AccrualRequest(userUuid, amount, "system");
        }
    }
}
