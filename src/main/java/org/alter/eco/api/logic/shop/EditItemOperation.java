package org.alter.eco.api.logic.shop;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.alter.eco.api.exception.HttpCodeException;
import org.alter.eco.api.jooq.tables.records.ItemRecord;
import org.alter.eco.api.logic.shop.CreateItemOperation.ItemAttachPhotosRequest;
import org.alter.eco.api.service.db.ItemAttachmentService;
import org.alter.eco.api.service.db.ShopService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.alter.eco.api.exception.ApplicationError.INTERNAL_ERROR;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class EditItemOperation {

    private final static Logger log = LoggerFactory.getLogger(EditItemOperation.class);

    private final ShopService shopService;
    private final ItemAttachmentService attachmentService;

    public void process(EditItemRequest request) {
        log.info("EditItemOperation.process.in request = {}", request);
        try {
            internalProcess(request);
        }  catch (HttpCodeException e) {
            log.error("EditItemOperation.process.thrown", e);
            throw e;
        } catch (Exception e) {
            log.error("EditItemOperation.process.thrown", e);
            throw INTERNAL_ERROR.exception(e);
        }
        log.info("EditItemOperation.process.out");
    }

    private void internalProcess(EditItemRequest request) {
        var id = request.newItem.getId();
        if (request.detach) {
            attachmentService.detach(id);
        }
        var attachmentRecords = request.attachPhotosRequest.stream()
            .map(ItemAttachPhotosRequest::asRecord)
            .collect(toList());

        shopService.update(request.newItem);
        attachmentService.attach(attachmentRecords);
    }

    @ToString(exclude = "attachPhotosRequest")
    @RequiredArgsConstructor
    public static class EditItemRequest {

        public final ItemRecord newItem;
        public final List<ItemAttachPhotosRequest> attachPhotosRequest;
        public final boolean detach;
    }
}
