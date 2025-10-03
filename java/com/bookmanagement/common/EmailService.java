package com.bookmanagement.common;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendOtpEmail(String to, String otp) {
        // For demonstration, print OTP to console
        // To enable email, configure JavaMail and replace this implementation
        System.out.println("=== PASSWORD RESET OTP ===");
        System.out.println("Email: " + to);
        System.out.println("OTP: " + otp);
        System.out.println("This OTP will expire in 10 minutes.");
        System.out.println("==========================");
    }
}