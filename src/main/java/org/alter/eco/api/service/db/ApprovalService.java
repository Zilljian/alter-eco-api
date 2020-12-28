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
        return db.selectFrom(approvalTable)
            .where(approvalTable.TASK_ID.equal(taskId))
            .fetchOptional();
    }

    public void insertTaskVotePlus(VoteForTaskRequest request) {
        db.update(approvalTable)
            .set(approvalTable.COUNTER, approvalTable.COUNTER.add(1))
            .where(approvalTable.TASK_ID.equal(request.taskId()))
            .execute();
    }

    public void insertTaskVoteMinus(VoteForTaskRequest request) {
        db.update(approvalTable)
            .set(approvalTable.COUNTER, approvalTable.COUNTER.sub(1))
            .where(approvalTable.TASK_ID.equal(request.taskId()))
            .execute();
    }

    public void insertUserVote(VoteForTaskRequest request) {
        db.insertInto(voteTable)
            .set(request.voteRecord())
            .execute();
    }

    public void insertOnConflictUpdate(ChangingStatus request) {
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
    }

    public List<ApprovalRecord> findTasksForTrashing(FindByTasksForTrashingRequest request) {
        return db.deleteFrom(approvalTable)
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
    }

    public List<ApprovalRecord> findTasksForApproving(FindByTimeShiftAndCounterRequest request) {
        return db.deleteFrom(approvalTable)
            .where(approvalTable.COUNTER.greaterOrEqual(request.counterThreshold()))
            .and(approvalTable.STATUS.equal(request.status()))
            .returning(approvalTable.TASK_ID, approvalTable.STATUS)
            .fetch();
    }

    public List<String> findClientIdsForAccrual(FindClientIdsForAccrualRequest request) {
        return db.deleteFrom(voteTable)
            .where(voteTable.TASK_ID.equal(request.taskId()))
            .and(voteTable.TYPE.equal(request.type()))
            .returning(voteTable.CLIENT_ID)
            .fetch()
            .map(VoteRecord::getClientId);
    }

    public void deleteUserVotes(Long taskId) {
        db.deleteFrom(voteTable)
            .where(voteTable.TASK_ID.equal(taskId))
            .execute();
    }
}
