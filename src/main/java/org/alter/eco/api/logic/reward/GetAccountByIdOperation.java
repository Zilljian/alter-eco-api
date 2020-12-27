package org.alter.eco.api.logic.reward;

import lombok.RequiredArgsConstructor;
import org.alter.eco.api.jooq.tables.records.AccountRecord;
import org.alter.eco.api.logic.task.CreateTaskOperation.CreateTaskRequest;
import org.alter.eco.api.service.db.RewardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNullElse;
import static org.alter.eco.api.exception.ApplicationError.ACCOUNT_NOT_FOUND_BY_ID;

@Component
@RequiredArgsConstructor
public class GetAccountByIdOperation {

    private final static Logger log = LoggerFactory.getLogger(GetAccountByIdOperation.class);

    private final RewardService rewardService;

    public AccountRecord process() {
        log.info("GetAccountByIdOperation.process.in");
        var result = internalProcess();
        log.info("GetAccountByIdOperation.process.out");
        return result;
    }

    private AccountRecord internalProcess() {
        var userUuid = requireNonNullElse(MDC.get("user"), "default");
        return rewardService.findByUser(userUuid)
            .orElseThrow(() -> ACCOUNT_NOT_FOUND_BY_ID.exception("No such account exist for client id = " + userUuid));
    }
}
