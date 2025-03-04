package com.safetypin.authentication.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private static final String SENDER = "noreply@safetyp.in";

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean sendOTPMail(String to, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(SENDER);
        message.setTo(to);
        message.setSubject("OTP Code for SafetyPin");
        message.setText("This is your OTP for recovery/verifying:\n\n" + otp);

        try {
            mailSender.send(message);
        } catch (MailException e) {
            logger.warn("EmailService.sendOTPMail:: Failed to send mail with error; {}", e.getMessage());
            return false;
        }
        return true;
    }
}
