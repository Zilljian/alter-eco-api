package org.alter.eco.api.service.db;

import lombok.RequiredArgsConstructor;
import org.alter.eco.api.jooq.enums.TaskStatus;
import org.alter.eco.api.jooq.tables.Approval;
import org.alter.eco.api.jooq.tables.Vote;
import org.alter.eco.api.jooq.tables.records.ApprovalRecord;
import org.alter.eco.api.jooq.tables.records.VoteRecord;
import org.alter.eco.api.logic.approval.ApproveScheduledOperation.FindByTasksForTrashingRequest;
import org.alter.eco.api.logic.approval.ApproveScheduledOperation.FindByTimeShiftAndCounterRequest;
import org.alter.eco.api.logic.approval.ApproveScheduledOperation.FindClientIdsForAccrualRequest;
import org.alter.eco.api.logic.approval.VoteForTaskOperation.VoteForTaskRequest;
import org.alter.eco.api.model.ChangingStatus;
import org.jooq.DSLContext;
import org.jooq.types.DayToSecond;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApprovalService {

    private static final Logger log = LoggerFactory.getLogger(ApprovalService.class);

    private final DSLContext db;

    private final Approval approvalTable = Approval.APPROVAL;
    private final Vote voteTable = Vote.VOTE;

    public Optional<ApprovalRecord> findByTaskId(Long taskId) {
        log.info("ApprovalService.findByTaskId.in taskId = {}", taskId);
        var result = db.selectFrom(approvalTable)
            .where(approvalTable.TASK_ID.equal(taskId))
            .fetchOptional();
        log.info("ApprovalService.findByTaskId.out");
        return result;
    }

    public void insertTaskVotePlus(VoteForTaskRequest request) {
        log.info("ApprovalService.insertTaskVotePlus.in request = {}", request);
        db.update(approvalTable)
            .set(approvalTable.COUNTER, approvalTable.COUNTER.add(1))
            .where(approvalTable.TASK_ID.equal(request.taskId()))
            .execute();
        log.info("ApprovalService.insertTaskVotePlus.out");
    }

    public void insertTaskVoteMinus(VoteForTaskRequest request) {
        log.info("ApprovalService.insertTaskVoteMinus.in request = {}", request);
        db.update(approvalTable)
            .set(approvalTable.COUNTER, approvalTable.COUNTER.sub(1))
            .where(approvalTable.TASK_ID.equal(request.taskId()))
            .execute();
        log.info("ApprovalService.insertTaskVoteMinus.out");
    }

    public void insertUserVote(VoteForTaskRequest request) {
        log.info("ApprovalService.insertUserVote.in request = {}", request);
        db.insertInto(voteTable)
            .set(request.voteRecord())
            .execute();
        log.info("ApprovalService.insertUserVote.out");
    }

    public void insertOnConflictUpdate(ChangingStatus request) {
        log.info("ApprovalService.insertOnConflictUpdate.in request = {}", request);
        var approval = new ApprovalRecord();
        approval.setTaskId(request.taskId());
        approval.setStatus(request.status());

        db.insertInto(approvalTable)
            .set(approval)
            .onConflictOnConstraint(approvalTable.getPrimaryKey())
            .doUpdate()
            .set(approvalTable.STATUS, request.status())
            .where(approvalTable.TASK_ID.equal(request.taskId()))
            .execute();
        log.info("ApprovalService.insertOnConflictUpdate.out");
    }

    public List<ApprovalRecord> findTasksForTrashing(FindByTasksForTrashingRequest request) {
        log.info("ApprovalService.findTasksForTrashing.in request = {}", request);
        var result = db.deleteFrom(approvalTable)
            .where(
                approvalTable.CREATED.add(DayToSecond.minute(String.valueOf(request.approveMinutesThreshold()))).lessOrEqual(LocalDateTime.now())
                    .and(approvalTable.COUNTER.greaterOrEqual(request.approveCountThreshold()))
                    .and(approvalTable.STATUS.equal(TaskStatus.WAITING_FOR_APPROVE))
            ).or(
                approvalTable.CREATED.add(DayToSecond.minute(String.valueOf(request.completeMinutesThreshold()))).lessOrEqual(LocalDateTime.now())
                    .and(approvalTable.COUNTER.greaterOrEqual(request.completeCountThreshold()))
                    .and(approvalTable.STATUS.equal(TaskStatus.RESOLVED))
            )
            .returning(approvalTable.TASK_ID, approvalTable.STATUS)
            .fetch();
        log.info("ApprovalService.findTasksForTrashing.out result = {}", result);
        return result;
    }

    public List<ApprovalRecord> findTasksForApproving(FindByTimeShiftAndCounterRequest request) {
        log.info("ApprovalService.findTasksForApproving.in request = {}", request);
        var result = db.deleteFrom(approvalTable)
            .where(approvalTable.COUNTER.greaterOrEqual(request.counterThreshold()))
            .and(approvalTable.STATUS.equal(request.status()))
            .returning(approvalTable.TASK_ID, approvalTable.STATUS)
            .fetch();
        log.info("ApprovalService.findTasksForApproving.out result = {}", result);
        return result;
    }

    public List<String> findClientIdsForAccrual(FindClientIdsForAccrualRequest request) {
        log.info("ApprovalService.findClientIdsForAccrual.in taskId = {}", request);
        var result = db.deleteFrom(voteTable)
            .where(voteTable.TASK_ID.equal(request.taskId()))
            .and(voteTable.TYPE.equal(request.type()))
            .returning(voteTable.CLIENT_ID)
            .fetch()
            .map(VoteRecord::getClientId);
        log.info("ApprovalService.findClientIdsForAccrual.out result = {}", result);
        return result;
    }

    public void deleteUserVotes(Long taskId) {
        log.info("ApprovalService.deleteUserVotes.in taskId = {}", taskId);
        db.deleteFrom(voteTable)
            .where(voteTable.TASK_ID.equal(taskId))
            .execute();
        log.info("ApprovalService.deleteUserVotes.out");
    }
}
