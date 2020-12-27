package org.alter.eco.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.alter.eco.api.logic.shop.CreateItemOperation;
import org.alter.eco.api.logic.shop.CreateItemOperation.CreateItemRequest;
import org.alter.eco.api.logic.shop.CreateItemOperation.ItemAttachPhotosRequest;
import org.alter.eco.api.logic.shop.EditItemOperation;
import org.alter.eco.api.logic.shop.EditItemOperation.EditItemRequest;
import org.alter.eco.api.logic.shop.FindAttachmentsByItemIdOperation;
import org.alter.eco.api.logic.shop.FindItemAttachmentByIdOperation;
import org.alter.eco.api.logic.shop.FindItemByIdOperation;
import org.alter.eco.api.logic.shop.FindItemsByUserOperation;
import org.alter.eco.api.logic.shop.FindItemsOperation;
import org.alter.eco.api.logic.shop.FindItemsOperation.FindItemsRequest;
import org.alter.eco.api.logic.shop.PurchaseItemOperation;
import org.alter.eco.api.model.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import javax.validation.Valid;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ShopController {

    private final static Logger log = LoggerFactory.getLogger(ShopController.class);

    private final ControllerHelper helper;

    private final CreateItemOperation createItemOperation;
    private final FindItemsOperation findItemsOperation;
    private final FindItemByIdOperation findItemByIdOperation;
    private final FindItemAttachmentByIdOperation findItemAttachmentByIdOperation;
    private final FindAttachmentsByItemIdOperation findAttachmentsByItemIdOperation;
    private final EditItemOperation editItemOperation;
    private final PurchaseItemOperation purchaseItemOperation;
    private final FindItemsByUserOperation findItemsByUserOperation;

    @PostMapping("/items")
    @Operation(summary = "Find all shop items that satisfy the conditions.")
    public List<Item> findItems(
        @Parameter(description = "'searchString' allows fulltext search in title field.")
        @Valid @RequestBody FindItemsRequest request,
        @RequestHeader("Authorization") String token) {
        log.info("ShopController.findItems.in request = {}", request);
        helper.obtainToken(token);
        var result = findItemsOperation.process(request);
        log.info("ShopController.findItems.out");
        return result;
    }

    @GetMapping("/item/{id}")
    @Operation(summary = "Get shop item by it's id.")
    public Item getItemById(@PathVariable(value = "id") Long id,
                            @RequestHeader("Authorization") String token) {
        log.info("ShopController.getItemById.in id = {}", id);
        helper.obtainToken(token);
        var result = findItemByIdOperation.process(id);
        log.info("ShopController.getItemById.out");
        return result;
    }

    @PostMapping(value = "/item",
        consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE
        })
    @Operation(summary = "Create new shop item with attachments. Allowed only for admin user.")
    public Long createItem(@RequestPart("item") Item Item,
                           @RequestPart(value = "attachment", required = false) List<MultipartFile> attachment,
                           @RequestHeader("Authorization") String token) {
        log.info("ShopController.createItem.in Item = {}", Item);
        helper.obtainToken(token);
        var attachments = ofNullable(attachment).orElse(List.of()).stream()
            .map(a -> {
                try {
                    return new ItemAttachPhotosRequest(a.getContentType(), a.getSize(), a.getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).collect(toList());
        var request = new CreateItemRequest(Item.toRecord(), attachments);
        var result = createItemOperation.process(request);
        log.info("ShopController.createItem.out");
        return result;
    }

    @PutMapping(value = "/item", params = "detach")
    @Operation(summary = "Edit existed shop item. Allowed only for admin user.")
    public void editItem(@RequestPart("ttem") Item item,
                         @RequestPart(value = "attachment", required = false) List<MultipartFile> attachment,
                         @Parameter(description = "Whether delete or not delete attached photos.")
                         @RequestParam(value = "detach") boolean detach,
                         @RequestHeader("Authorization") String token) {
        log.info("ShopController.editItem.in Item = {}", item);
        helper.obtainToken(token);
        var attachments = ofNullable(attachment).orElse(List.of()).stream()
            .map(a -> {
                try {
                    return new ItemAttachPhotosRequest(item.id(), a.getContentType(), a.getSize(), a.getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).collect(toList());
        var request = new EditItemRequest(item.toRecord(), attachments, detach);
        editItemOperation.process(request);
        log.info("ShopController.editItem.out");
    }

    @GetMapping(value = "/item/{id}/attachment", produces = {
        MediaType.MULTIPART_FORM_DATA_VALUE
    })
    @ResponseBody
    @Operation(summary = "Get shop item's attachments by item's id.")
    public MultiValueMap<String, HttpEntity<?>> findAttachmentsByItemId(
        @Parameter(description = "Shop item's id.")
        @PathVariable(value = "id") Long itemId,
        @RequestHeader("Authorization") String token) {
        log.info("ShopController.findAttachments.in id = {}", itemId);
        helper.obtainToken(token);
        var result = findAttachmentsByItemIdOperation.process(itemId);
        var builder = new MultipartBodyBuilder();
        result.forEach(a -> builder.part(
            "attachment",
            a.getContent(),
            MediaType.valueOf(a.getType())
        ));
        var response = builder.build();
        log.info("ShopController.findAttachments.out");
        return response;
    }

    @GetMapping(value = "/item/{id}/purchase")
    @Operation(summary = "Purchase item with certain id. User obtained from token.")
    public void purchaseItem(
        @Parameter(description = "Shop item's id")
        @PathVariable(value = "id") Long itemId,
        @RequestHeader("Authorization") String token) {
        log.info("ShopController.purchaseItem.in itemId = {}", itemId);
        helper.obtainToken(token);
        purchaseItemOperation.process(itemId);
        log.info("ShopController.purchaseItem.out");
    }

    @GetMapping(value = "/items")
    @ResponseBody
    @Operation(summary = "Find shop items that have been purchased by user. User obtained from token.")
    public List<Item> findItemsByUser(@RequestHeader("Authorization") String token) {
        log.info("ShopController.findItemsByUser.in");
        helper.obtainToken(token);
        var result = findItemsByUserOperation.process();
        log.info("ShopController.findItemsByUser.out");
        return result;
    }

    @GetMapping(value = "/item/attachment/{id}",
        produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE})
    @ResponseBody
    @Operation(summary = "Get certain shop item's attachment by it's id.")
    public byte[] findAttachmentsById(
        @Parameter(description = "Attachment's id.")
        @PathVariable(value = "id") Long id,
        @RequestHeader("Authorization") String token) {
        log.info("ShopController.findAttachmentsById.in id = {}", id);
        helper.obtainToken(token);
        var result = findItemAttachmentByIdOperation.process(id).getContent();
        log.info("ShopController.findAttachmentsById.out");
        return result;
    }
}
