package com.tradeops.repository;

import com.tradeops.domain.WorkflowTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowTaskRepository extends JpaRepository<WorkflowTask, Long> {
}
