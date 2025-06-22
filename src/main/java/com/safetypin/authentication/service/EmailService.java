package com.safetypin.authentication.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String SENDER = "noreply@safetyp.in";
    private final JavaMailSender mailSender;


    @Autowired
    public EmailService(
            JavaMailSender mailSender,
            @Value("${mail.proxy.host}") String proxyHost,
            @Value("${mail.proxy.port}") String proxyPort,
            @Value("${mail.proxy.enabled}") boolean proxyEnabled) {
        // Configure the proxy settings for JavaMail
        if (proxyEnabled && mailSender instanceof JavaMailSenderImpl javaMailSender) {
            Properties props = javaMailSender.getJavaMailProperties();
            props.put("mail.smtp.proxy.host", proxyHost);
            props.put("mail.smtp.proxy.port", proxyPort);
            logger.info("EmailService:: Configured SMTP proxy settings - host: {}, port: {}", proxyHost, proxyPort);
        }

        this.mailSender = mailSender;

        logger.info("EmailService:: initialized with JavaMailSender: {}", mailSender);
    }

    @Async("emailTaskExecutor")
    public CompletableFuture<Boolean> sendOTPMail(String to, String otp) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom(SENDER);
            helper.setTo(to);
            helper.setSubject("OTP Code for SafetyPin");

            String htmlContent =
                    "<!DOCTYPE html>" +
                            "<html>" +
                            "<head>" +
                            "    <style>" +
                            "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                            "        .container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px; }" +
                            "        .header { background-color: #4285f4; color: white; padding: 10px; text-align: center; border-radius: 5px 5px 0 0; }" +
                            "        .content { padding: 20px; }" +
                            "        .otp-code { font-size: 24px; font-weight: bold; text-align: center; margin: 20px 0; padding: 10px; background-color: #f0f0f0; border-radius: 4px; letter-spacing: 5px; }" +
                            "        .footer { font-size: 12px; color: #777; text-align: center; margin-top: 20px; }" +
                            "    </style>" +
                            "</head>" +
                            "<body>" +
                            "    <div class='container'>" +
                            "        <div class='header'>" +
                            "            <h2>SafetyPin Security</h2>" +
                            "        </div>" +
                            "        <div class='content'>" +
                            "            <p>Hello,</p>" +
                            "            <p>Your one-time verification code is:</p>" +
                            "            <div class='otp-code'>" + otp + "</div>" +
                            "            <p>This code will expire in 2 minutes. Please do not share this code with anyone.</p>" +
                            "            <p>If you didn't request this code, please ignore this email.</p>" +
                            "        </div>" +
                            "        <div class='footer'>" +
                            "            <p>This is an automated message. Please do not reply.</p>" +
                            "            <p>&copy; " + java.time.Year.now().getValue() + " SafetyPin. All rights reserved.</p>" +
                            "        </div>" +
                            "    </div>" +
                            "</body>" +
                            "</html>";

            helper.setText(htmlContent, true); // true indicates HTML content

            mailSender.send(mimeMessage);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            logger.warn("EmailService.sendOTPMail:: Failed to send mail with error; {}", e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }


    public void testConnection() {
        try {
            logger.info("{}", ((JavaMailSenderImpl) mailSender).getJavaMailProperties());
            // testConnection
            ((JavaMailSenderImpl) mailSender).testConnection();
        } catch (Exception e) {
            logger.warn("EmailService:: Init Error:", e);
        }
    }
}
