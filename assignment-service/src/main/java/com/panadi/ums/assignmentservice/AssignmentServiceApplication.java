package com.panadi.ums.assignmentservice;

import com.panadi.ums.auditcommon.AuditOutboxConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
@org.springframework.context.annotation.Import(AuditOutboxConfiguration.class)
public class AssignmentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssignmentServiceApplication.class, args);
    }

}
