package com.panadi.ums.attendanceservice;

import com.panadi.ums.auditcommon.AuditOutboxConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
@org.springframework.context.annotation.Import(AuditOutboxConfiguration.class)
public class AttendanceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttendanceServiceApplication.class, args);
    }

}
