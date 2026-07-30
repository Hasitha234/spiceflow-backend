package com.spiceflow.backend;

import com.spiceflow.backend.sales.repository.MorningSummaryRepository;
import com.spiceflow.backend.sales.entity.MorningSummary;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CheckDataRunner implements CommandLineRunner {
    private final MorningSummaryRepository repo;
    public CheckDataRunner(MorningSummaryRepository repo) { this.repo = repo; }
    
    @Override
    @Transactional
    public void run(String... args) {
        repo.findAll().forEach(ms -> {
            System.out.println("MS: " + ms.getSummaryNumber() + ", Est: " + ms.getFinalEstimateValue());
            ms.getItems().forEach(i -> {
                System.out.println("  Item: " + i.getProduct().getName() + ", Qty: " + i.getQuantity() + ", Ret: " + i.getExpectedReturnAmount());
            });
        });
    }
}
