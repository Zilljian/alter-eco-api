package org.alter.eco.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.alter.eco.api.logic.reward.GetAccountByIdOperation;
import org.alter.eco.api.model.Account;
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
    @Operation(
        description = "Get account that attached to user. If account doesnt exist this operation creates new empty account and return it.",
        summary = "Get user account"
    )
    public Account getAccountByUser(@RequestHeader("Authorization") String token) {
        log.info("RewardController.getAccountById.in");
        helper.obtainToken(token);
        var result = getAccountByIdOperation.process();
        log.info("RewardController.getAccountById.out");
        return Account.fromRecord(result);
    }
}
