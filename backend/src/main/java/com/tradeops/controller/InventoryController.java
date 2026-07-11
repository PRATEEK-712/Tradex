package com.tradeops.controller;

import com.tradeops.domain.InventoryItem;
import com.tradeops.repository.InventoryItemRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController extends CrudController<InventoryItem> {
    public InventoryController(InventoryItemRepository repository) {
        super(repository, "/api/inventory");
    }
}
