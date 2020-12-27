package org.alter.eco.api.logic.shop;

import lombok.RequiredArgsConstructor;
import org.alter.eco.api.jooq.tables.records.ItemAttachmentRecord;
import org.alter.eco.api.model.Item;
import org.alter.eco.api.service.db.ItemAttachmentService;
import org.alter.eco.api.service.db.ShopService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.toList;
import static org.alter.eco.api.exception.ApplicationError.NOT_FOUND_BY_ID;

@Component
@RequiredArgsConstructor
public class FindItemByIdOperation {

    private final static Logger log = LoggerFactory.getLogger(FindItemByIdOperation.class);

    private final ShopService shopService;
    private final ItemAttachmentService attachmentService;

    public Item process(Long id) {
        log.info("FindItemByIdOperation.process.in id = {}", id);
        var result = internalProcess(id);
        log.info("FindItemByIdOperation.process.out");
        return result;
    }

    private Item internalProcess(Long id) {
        var item = shopService.findById(id)
            .orElseThrow(() -> NOT_FOUND_BY_ID.exception("No such item exist with id = " + id));
        var attachments = attachmentService.findIdsByItemId(item.getId()).stream()
            .map(ItemAttachmentRecord::getId)
            .collect(toList());
        return Item.of(item, attachments);
    }
}
