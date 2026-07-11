package com.tradeops.controller;

import com.tradeops.domain.StaffMember;
import com.tradeops.repository.StaffMemberRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff")
public class StaffController extends CrudController<StaffMember> {
    public StaffController(StaffMemberRepository repository) {
        super(repository, "/api/staff");
    }
}
