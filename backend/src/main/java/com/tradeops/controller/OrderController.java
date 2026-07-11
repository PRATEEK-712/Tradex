package com.tradeops.controller;

import com.tradeops.domain.CustomerOrder;
import com.tradeops.repository.CustomerOrderRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController extends CrudController<CustomerOrder> {
    public OrderController(CustomerOrderRepository repository) {
        super(repository, "/api/orders");
    }
}
