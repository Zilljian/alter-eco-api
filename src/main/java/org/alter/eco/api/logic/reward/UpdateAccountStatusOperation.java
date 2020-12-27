package org.alter.eco.api.logic.reward;

import lombok.RequiredArgsConstructor;
import org.alter.eco.api.jooq.enums.AccountStatus;
import org.alter.eco.api.service.db.RewardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static org.alter.eco.api.exception.ApplicationError.INTERNAL_ERROR;

@Component
@RequiredArgsConstructor
public class UpdateAccountStatusOperation {

    private final static Logger log = LoggerFactory.getLogger(UpdateAccountStatusOperation.class);

    public final RewardService rewardService;

    public void process(UpdateStatusRequest request) {
        log.info("UpdateAccountStatusOperation.process.in id = {}", request);
        try {
            internalProcess(request);
        } catch (Exception e) {
            log.warn("UpdateAccountStatusOperation.process.thrown", e);
            throw INTERNAL_ERROR.exception(e);
        }
        log.info("UpdateAccountStatusOperation.process.out");
    }

    private void internalProcess(UpdateStatusRequest request) {
        rewardService.updateStatus(request);
    }

    public static record UpdateStatusRequest(String userUuid, AccountStatus status) {}
}
