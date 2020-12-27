package org.alter.eco.api.controller;

import lombok.RequiredArgsConstructor;
import org.alter.eco.api.jooq.tables.records.AccountRecord;
import org.alter.eco.api.logic.reward.GetAccountByIdOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RewardController {

    private final static Logger log = LoggerFactory.getLogger(RewardController.class);

    private final ControllerHelper helper;

    private final GetAccountByIdOperation getAccountByIdOperation;

    @GetMapping(value = "/account")
    public AccountRecord getAccountByUser(@RequestHeader("Authorization") String token) {
        log.info("RestController.getAccountById.in");
        helper.obtainToken(token);
        var result = getAccountByIdOperation.process();
        log.info("RestController.getAccountById.out");
        return result;
    }
}
