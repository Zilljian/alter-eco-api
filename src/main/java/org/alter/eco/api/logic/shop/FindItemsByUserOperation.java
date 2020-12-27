package org.alter.eco.api.logic.shop;

import lombok.RequiredArgsConstructor;
import org.alter.eco.api.model.Item;
import org.alter.eco.api.service.db.ShopService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public class FindItemsByUserOperation {

    private final static Logger log = LoggerFactory.getLogger(FindItemsByUserOperation.class);

    private final ShopService shopService;

    public List<Item> process() {
        log.info("FindItemsByUserOperation.process.in");
        var result = internalProcess();
        log.info("FindItemsByUserOperation.process.out");
        return result;
    }

    private List<Item> internalProcess() {
        var userUuid = requireNonNullElse(MDC.get("user"), "default");
        return shopService.findByUser(userUuid).stream()
            .map(Item::of)
            .collect(toList());
    }
}
