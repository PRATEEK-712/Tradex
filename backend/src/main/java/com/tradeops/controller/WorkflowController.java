package com.tradeops.controller;

import com.tradeops.domain.WorkflowTask;
import com.tradeops.repository.WorkflowTaskRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController extends CrudController<WorkflowTask> {
    public WorkflowController(WorkflowTaskRepository repository) {
        super(repository, "/api/workflows");
    }
}
