package org.alter.eco.api.logic.shop;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.alter.eco.api.jooq.tables.records.ItemAttachmentRecord;
import org.alter.eco.api.jooq.tables.records.ItemRecord;
import org.alter.eco.api.service.db.ItemAttachmentService;
import org.alter.eco.api.service.db.ShopService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.alter.eco.api.exception.ValidationError.INVALID_ATTACH_REQUEST;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class CreateItemOperation {

    private final static Logger log = LoggerFactory.getLogger(CreateItemOperation.class);

    private final ShopService shopService;
    private final ItemAttachmentService attachmentService;

    public Long process(CreateItemRequest request) {
        log.info("CreateItemOperation.process.in id = {}", request);
        var result = internalProcess(request);
        log.info("CreateItemOperation.process.out");
        return result;
    }

    private Long internalProcess(CreateItemRequest request) {
        var itemId = shopService.insert(request.newItem).getId();
        var attachPhotos = request.attachPhotosRequest.stream()
            .peek(a -> a.setItemId(itemId))
            .map(ItemAttachPhotosRequest::validate)
            .map(ItemAttachPhotosRequest::asRecord)
            .collect(toList());
        attachmentService.attach(attachPhotos);
        return itemId;
    }

    @ToString(exclude = "attachPhotosRequest")
    @RequiredArgsConstructor
    public static class CreateItemRequest {

        public final ItemRecord newItem;
        public final List<ItemAttachPhotosRequest> attachPhotosRequest;
    }

    @Setter
    @ToString
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class ItemAttachPhotosRequest {

        private Long itemId;
        public final String contentType;
        public final Long contentLength;
        public final byte[] content;

        public ItemAttachPhotosRequest validate() {
            if (isNull(contentType)) {
                throw INVALID_ATTACH_REQUEST.exception("Content-Type cannot be null");
            } else if (isNull(content)) {
                throw INVALID_ATTACH_REQUEST.exception("Content cannot be null");
            }
            return this;
        }

        public ItemAttachmentRecord asRecord() {
            var r = new ItemAttachmentRecord();
            r.setItemId(itemId);
            r.setType(contentType);
            r.setContent(content);
            r.setLength(contentLength);
            return r;
        }
    }
}
