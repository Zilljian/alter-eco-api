package org.alter.eco.api.logic.shop;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.alter.eco.api.jooq.tables.Item;
import org.alter.eco.api.jooq.tables.Task;
import org.alter.eco.api.jooq.tables.records.ItemRecord;
import org.alter.eco.api.service.db.ItemAttachmentService;
import org.alter.eco.api.service.db.ShopService;
import org.jooq.Condition;
import org.jooq.SelectForUpdateStep;
import org.jooq.SelectWhereStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public class FindItemsOperation {

    private final static Logger log = LoggerFactory.getLogger(FindItemsOperation.class);

    private final ShopService shopService;
    private final ItemAttachmentService attachmentService;

    public List<org.alter.eco.api.model.Item> process(FindItemsRequest request) {
        log.info("FindItemsOperation.process.in request = {}", request);
        var result = internalProcess(request);
        log.info("FindItemsOperation.process.out");
        return result;
    }

    private List<org.alter.eco.api.model.Item> internalProcess(FindItemsRequest request) {
        var items = shopService.findByFilters(request);
        var itemIds = items.stream().map(ItemRecord::getId).collect(toList());
        var attachments = attachmentService.findIdsByItemIds(itemIds);
        return items.stream()
            .map(t -> org.alter.eco.api.model.Item.of(t, attachments.get(t.getId())))
            .collect(toList());
    }

    public static record FindItemsRequest(
        @JsonProperty("createdBy") String createdBy,
        @JsonProperty("searchString") String searchString,
        @JsonProperty("offset") Long offset,
        @JsonProperty("limit") Long limit) {

        @JsonCreator
        public FindItemsRequest {
        }

        public SelectForUpdateStep<ItemRecord> withCondition(SelectWhereStep<ItemRecord> query) {
            Condition condition = null;
            if (nonNull(searchString)) {
                condition = Item.ITEM.TITLE.like(format("%s%%", searchString));
            }
            if (nonNull(createdBy)) {
                condition = condition == null ?
                            Task.TASK.CREATED_BY.equal(createdBy) :
                            condition.and(Task.TASK.CREATED_BY.equal(createdBy));
            }

            if (condition != null) {
                query.where(condition);
            }

            return query
                .limit(requireNonNullElse(limit, 20L))
                .offset(requireNonNullElse(offset, 0L));
        }
    }
}
