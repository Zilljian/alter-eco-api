package org.alter.eco.api.logic.shop;

import lombok.RequiredArgsConstructor;
import org.alter.eco.api.exception.HttpCodeException;
import org.alter.eco.api.logic.reward.WriteoffByUserIdOperation;
import org.alter.eco.api.logic.reward.WriteoffByUserIdOperation.WriteoffRequest;
import org.alter.eco.api.service.db.ShopService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNullElse;
import static org.alter.eco.api.exception.ApplicationError.INTERNAL_ERROR;
import static org.alter.eco.api.exception.ApplicationError.NOT_ENOUGH_AMOUNT;
import static org.alter.eco.api.exception.ApplicationError.NOT_FOUND_BY_ID;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class PurchaseItemOperation {

    private final static Logger log = LoggerFactory.getLogger(FindItemsOperation.class);

    private final ShopService shopService;
    private final WriteoffByUserIdOperation writeoffByUserIdOperation;

    public void process(Long itemId) {
        log.info("PurchaseItemOperation.process.in itemId = {}", itemId);
        try {
            internalProcess(itemId);
        }  catch (HttpCodeException e) {
            log.error("PurchaseItemOperation.process.thrown", e);
            throw e;
        } catch (Exception e) {
            log.error("PurchaseItemOperation.process.thrown", e);
            throw INTERNAL_ERROR.exception(e);
        }
        log.info("PurchaseItemOperation.process.out");
    }

    private void internalProcess(Long itemId) {
        var userUuid = requireNonNullElse(MDC.get("user"), "default");
        var item = shopService.findById(itemId)
            .orElseThrow(() -> NOT_FOUND_BY_ID.exception("No such item exist with id = " + itemId));
        if (item.getAmount() == 0) {
            throw NOT_ENOUGH_AMOUNT.exception("Not enough amount items for purchase");
        }
        writeoffByUserIdOperation.process(WriteoffRequest.system(userUuid, item.getPrice()));
        shopService.purchaseItem(userUuid, itemId);
    }
}
