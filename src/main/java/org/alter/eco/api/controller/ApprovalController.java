package org.alter.eco.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.alter.eco.api.jooq.enums.VoteType;
import org.alter.eco.api.logic.approval.VoteForTaskOperation;
import org.alter.eco.api.logic.approval.VoteForTaskOperation.VoteForTaskRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static java.util.Objects.requireNonNullElse;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApprovalController {

    private final static Logger log = LoggerFactory.getLogger(RestController.class);

    private final ControllerHelper helper;

    private final VoteForTaskOperation voteForTaskOperation;

    @PostMapping("/vote")
    @Operation(summary = "Vote for 'taskId'. Client obtained from token.")
    public void voteForTask(
        @RequestParam(value = "taskId") Long taskId,
        @Parameter(description = "voteType is either APPROVE or REJECT")
        @RequestParam(value = "type") VoteType voteType,
        @RequestHeader("Authorization") String token) {
        log.info("ApprovalController.voteForTask.in taskId = {}, voteType = {}", taskId, voteType);
        helper.obtainToken(token);
        var request = new VoteForTaskRequest(
            requireNonNullElse(MDC.get("user"), "default"),
            taskId,
            voteType
        );
        voteForTaskOperation.process(request);
        log.info("ApprovalController.voteForTask.out");
    }
}
