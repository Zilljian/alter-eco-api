package org.alter.eco.api.logic.approval;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.alter.eco.api.jooq.enums.TaskStatus;
import org.alter.eco.api.jooq.enums.VoteType;
import org.alter.eco.api.logic.reward.AccrualByClientIdOperation;
import org.alter.eco.api.logic.reward.AccrualByClientIdOperation.AccrualRequest;
import org.alter.eco.api.logic.task.UpdateTaskStatusOperation;
import org.alter.eco.api.logic.task.UpdateTaskStatusOperation.UpdateStatusRequest;
import org.alter.eco.api.service.db.ApprovalService;
import org.alter.eco.api.service.db.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.alter.eco.api.exception.ApplicationError.NOT_FOUND_BY_ID;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class ApproveScheduledOperation extends Thread {

    private static final Logger log = LoggerFactory.getLogger(ApproveScheduledOperation.class);

    @Value("${operation.approval.waiting-for-approve-m}")
    private Long waitingForApproveStatusShift;
    @Value("${operation.approval.resolved-m}")
    private Long resolvedStatusShift;
    @Value("${operation.approval.waiting-for-approve-count}")
    private Long waitingForApproveStatusCount;
    @Value("${operation.approval.resolved-count}")
    private Long resolvedStatusCount;
    @Value("${operation.approval.reward.approve-attendee}")
    private Long APPROVE_REWARD;
    @Value("${operation.approval.reward.complete-attendee}")
    private Long COMPLETE_REWARD;
    @Value("${operation.approval.reward.trash-attendee}")
    private Long TRASH_REWARD;
    @Value("${operation.approval.reward.creator}")
    private Long CREATOR_REWARD;

    private FindByTimeShiftAndCounterRequest approvingRequest;
    private FindByTimeShiftAndCounterRequest completingRequest;
    private FindByTasksForTrashingRequest trashingRequest;

    private final TaskService taskService;
    private final ApprovalService approvalService;

    private final AccrualByClientIdOperation accrualByClientIdOperation;
    private final UpdateTaskStatusOperation updateTaskStatusOperation;

    @EventListener(ApplicationStartedEvent.class)
    public void init() {
        this.approvingRequest = FindByTimeShiftAndCounterRequest.ofWaitingForApprove(waitingForApproveStatusShift, waitingForApproveStatusCount);
        this.completingRequest = FindByTimeShiftAndCounterRequest.ofResolved(resolvedStatusShift, resolvedStatusCount);
        this.trashingRequest = new FindByTasksForTrashingRequest(waitingForApproveStatusShift, resolvedStatusShift);
    }

    @Override
    @SneakyThrows
    public void run() {
        log.info("ApproveScheduledOperation.run.in");
        try {
            internalRun();
            log.info("ApproveScheduledOperation.run.out");
        } catch (Exception e) {
            log.error("ApproveScheduledOperation.run.thrown Pause executor for 30 seconds", e);
            Thread.sleep(30 * 1000);
        }
    }

    public void internalRun() {
        approvalService.findTasksForApproving(completingRequest).forEach(
            a -> {
                approvalService.findClientIdsForAccrual(request(a.getTaskId(), VoteType.APPROVE))
                    .forEach(u -> accrualByClientIdOperation.process(AccrualRequest.system(u, COMPLETE_REWARD)));
                updateTaskStatusOperation.process(new UpdateStatusRequest(a.getTaskId(), TaskStatus.COMPLETED));
                approvalService.deleteClients(a.getTaskId());
                var task = taskService.findById(a.getTaskId())
                    .orElseThrow(() -> NOT_FOUND_BY_ID.exception("No such task exist with id = " + a.getTaskId()));
                accrualByClientIdOperation.process(AccrualRequest.system(task.getAssignee(), task.getReward()));
                accrualByClientIdOperation.process(AccrualRequest.system(task.getCreatedBy(), CREATOR_REWARD));
            }
        );

        approvalService.findTasksForApproving(approvingRequest).forEach(
            a -> {
                approvalService.findClientIdsForAccrual(request(a.getTaskId(), VoteType.APPROVE))
                    .forEach(u -> accrualByClientIdOperation.process(AccrualRequest.system(u, APPROVE_REWARD)));
                updateTaskStatusOperation.process(new UpdateStatusRequest(a.getTaskId(), TaskStatus.TO_DO));
                approvalService.deleteClients(a.getTaskId());
            }
        );

        approvalService.findTasksForTrashing(trashingRequest).forEach(
            a -> {
                approvalService.findClientIdsForAccrual(request(a.getTaskId(), VoteType.REJECT))
                    .forEach(u -> accrualByClientIdOperation.process(AccrualRequest.system(u, TRASH_REWARD)));
                updateTaskStatusOperation.process(new UpdateStatusRequest(a.getTaskId(), TaskStatus.TRASHED));
                approvalService.deleteClients(a.getTaskId());
            }
        );
    }

    private FindClientIdsForAccrualRequest request(Long taskId, VoteType voteType) {
        return new FindClientIdsForAccrualRequest(taskId, voteType);
    }

    public static record FindByTimeShiftAndCounterRequest(Long shift, Long counter, TaskStatus status) {

        public static FindByTimeShiftAndCounterRequest ofWaitingForApprove(Long shift, Long counter) {
            return new FindByTimeShiftAndCounterRequest(shift, counter, TaskStatus.WAITING_FOR_APPROVE);
        }

        public static FindByTimeShiftAndCounterRequest ofResolved(Long shift, Long counter) {
            return new FindByTimeShiftAndCounterRequest(shift, counter, TaskStatus.RESOLVED);
        }
    }

    public static record FindByTasksForTrashingRequest(Long shift, Long completingShift) {}

    public static record FindClientIdsForAccrualRequest(Long taskId, VoteType type) {}
}
