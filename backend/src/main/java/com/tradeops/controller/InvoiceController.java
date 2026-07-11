package com.tradeops.controller;

import com.tradeops.domain.Invoice;
import com.tradeops.repository.InvoiceRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController extends CrudController<Invoice> {
    public InvoiceController(InvoiceRepository repository) {
        super(repository, "/api/invoices");
    }
}
