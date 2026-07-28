package com.panadi.ums.identityservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    void sendWelcomeEmail(String contactEmail, String firstName, String universityEmail, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(contactEmail);
        message.setSubject("Welcome to UMS! Your IT Account has been created");
        
        String body = String.format(
            "Dear %s,\n\n" +
            "Your university IT account has been created. Please find your login details below:\n\n" +
            "Email Address: %s\n" +
            "Temporary Password: %s\n\n" +
            "IMPORTANT: For security reasons, you will be required to change this password the very first time you log in.\n\n" +
            "Login Portal: http://localhost:5173\n",
            firstName, universityEmail, temporaryPassword
        );
        
        message.setText(body);
        message.setFrom("no-reply@ums.local");
        
        mailSender.send(message);
    }
}
