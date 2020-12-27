package org.alter.eco.api.logic.approval;

import lombok.RequiredArgsConstructor;
import org.alter.eco.api.jooq.enums.VoteType;
import org.alter.eco.api.jooq.tables.records.VoteRecord;
import org.alter.eco.api.service.db.ApprovalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static java.lang.String.format;
import static org.alter.eco.api.exception.ApplicationError.ENTRY_NOT_FOUND_BY_TASK_ID;
import static org.alter.eco.api.exception.ApplicationError.INTERNAL_ERROR;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class VoteForTaskOperation {

    private final static Logger log = LoggerFactory.getLogger(VoteForTaskOperation.class);

    private final ApprovalService approvalService;

    public void process(VoteForTaskRequest request) {
        log.info("VoteForTaskOperation.process.in id = {}", request);
        try {
            internalProcess(request);
        } catch (Exception e) {
            log.warn("VoteForTaskOperation.process.thrown", e);
            throw INTERNAL_ERROR.exception(e);
        }
        log.info("VoteForTaskOperation.process.out");
    }

    private void internalProcess(VoteForTaskRequest request) {
        approvalService.findByTaskId(request.taskId)
            .orElseThrow(() -> ENTRY_NOT_FOUND_BY_TASK_ID.exception(format("Entry with task id = %s not found", request.taskId)));
        approvalService.insertUserVote(request);
        if (request.type.equals(VoteType.APPROVE)) {
            approvalService.insertTaskVotePlus(request);
        } else {
            approvalService.insertTaskVoteMinus(request);
        }
    }

    public static record VoteForTaskRequest(String clientId, Long taskId, VoteType type) {

        public VoteRecord voteRecord() {
            return new VoteRecord(clientId, taskId, type);
        }
    }
}
