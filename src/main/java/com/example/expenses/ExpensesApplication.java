package com.example.expenses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExpensesApplication {

    private static final Logger log = LoggerFactory.getLogger(ExpensesApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ExpensesApplication.class, args);
        log.info("Expenses application started successfully. Access UI at http://localhost:8080/expenses and API at http://localhost:8080/api/v1/expenses");
    }
}

