package com.example.ats.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendApplicationStatusNotification(String toEmail, String applicantName, String jobTitle, String status) {
        String subject = "Your Job Application Status Update";
        String message = String.format(
            "Hello %s,\n\n" +
            "Your application for the job \"%s\" has been %s.\n\n" +
            "Thank you for using our ATS.\n\n" +
            "Best regards,\n" +
            "ATS Team",
            applicantName,
            jobTitle,
            status.toLowerCase()
        );

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(toEmail);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);

            System.out.println("📧 Sending email to: " + toEmail);
            mailSender.send(mailMessage);
            System.out.println("✅ Email sent successfully!");
        } catch (Exception e) {
            System.out.println("❌ Failed to send email:");
            e.printStackTrace();  // This is key to knowing the root cause
        }
    }}
