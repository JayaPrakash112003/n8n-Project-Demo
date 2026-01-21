package com.automation.zepto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ZeptoAutomationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZeptoAutomationApplication.class, args);
    }

}
