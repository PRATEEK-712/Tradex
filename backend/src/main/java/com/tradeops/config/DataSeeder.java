package com.tradeops.config;

import com.tradeops.domain.CustomerOrder;
import com.tradeops.domain.Department;
import com.tradeops.domain.InventoryItem;
import com.tradeops.domain.Invoice;
import com.tradeops.domain.StaffMember;
import com.tradeops.domain.Status;
import com.tradeops.domain.WorkflowTask;
import com.tradeops.repository.CustomerOrderRepository;
import com.tradeops.repository.InventoryItemRepository;
import com.tradeops.repository.InvoiceRepository;
import com.tradeops.repository.StaffMemberRepository;
import com.tradeops.repository.WorkflowTaskRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedData(
            InventoryItemRepository inventory,
            StaffMemberRepository staff,
            CustomerOrderRepository orders,
            InvoiceRepository invoices,
            WorkflowTaskRepository workflows) {
        return args -> {
            if (inventory.count() > 0) {
                return;
            }

            InventoryItem copper = new InventoryItem();
            copper.setSku("RM-COP-001");
            copper.setName("Copper Cathodes");
            copper.setCategory("Metals");
            copper.setQuantity(840);
            copper.setReorderPoint(250);
            copper.setUnitCost(new BigDecimal("715.50"));
            copper.setWarehouse("Mumbai Central");
            inventory.save(copper);

            InventoryItem cashew = new InventoryItem();
            cashew.setSku("AG-CAS-044");
            cashew.setName("Processed Cashew Kernels");
            cashew.setCategory("Agri Commodities");
            cashew.setQuantity(120);
            cashew.setReorderPoint(180);
            cashew.setUnitCost(new BigDecimal("89.75"));
            cashew.setWarehouse("Navi Mumbai");
            inventory.save(cashew);

            StaffMember ops = new StaffMember();
            ops.setFullName("Aarav Mehta");
            ops.setEmail("aarav.mehta@tradeops.local");
            ops.setDepartment(Department.OPERATIONS);
            ops.setRole("Operations Lead");
            staff.save(ops);

            StaffMember billing = new StaffMember();
            billing.setFullName("Nisha Rao");
            billing.setEmail("nisha.rao@tradeops.local");
            billing.setDepartment(Department.BILLING);
            billing.setRole("Billing Manager");
            staff.save(billing);

            CustomerOrder order = new CustomerOrder();
            order.setOrderNumber("SO-2026-1001");
            order.setCustomerName("Kaveri Retail Exports");
            order.setOrderDate(LocalDate.now().minusDays(2));
            order.setTotalAmount(new BigDecimal("184500.00"));
            order.setStatus(Status.IN_PROGRESS);
            orders.save(order);

            Invoice invoice = new Invoice();
            invoice.setInvoiceNumber("INV-2026-0881");
            invoice.setCustomerName("Kaveri Retail Exports");
            invoice.setDueDate(LocalDate.now().plusDays(10));
            invoice.setAmount(new BigDecimal("184500.00"));
            invoice.setStatus(Status.OPEN);
            invoices.save(invoice);

            WorkflowTask task = new WorkflowTask();
            task.setTitle("Approve dispatch documents for SO-2026-1001");
            task.setDepartment(Department.OPERATIONS);
            task.setOwner("Aarav Mehta");
            task.setDueDate(LocalDate.now().plusDays(1));
            task.setStatus(Status.IN_PROGRESS);
            workflows.save(task);
        };
    }
}
