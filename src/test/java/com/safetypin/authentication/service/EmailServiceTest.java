package com.safetypin.authentication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.ServerSetup;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

// Email integration test
@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class EmailServiceTest {
    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(
            new ServerSetup(2525, "127.0.0.1", "smtp"))
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("username", "secret123"))
            .withPerMethodLifecycle(false);

    @Autowired
    private EmailService emailService;

    @AfterEach
    void cleanup() throws FolderException {
        greenMail.purgeEmailFromAllMailboxes();
    }

    @Test
    void testSendOTPMail_Success() throws MessagingException, IOException, ExecutionException, InterruptedException {
        String otp = "123456";
        CompletableFuture<Boolean> future = emailService.sendOTPMail("username@test.com", otp);
        boolean status = future.get();
        assertTrue(status);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length);
        MimeMessage receivedMessage = receivedMessages[0];
        assertTrue(receivedMessage.getSubject().contains("OTP"));
        assertTrue(receivedMessage.getContent().toString().contains(otp));
    }

    @Test
    void testSendOTPMail_MailServerDown() throws ExecutionException, InterruptedException {
        greenMail.stop();

        String otp = "123456";
        CompletableFuture<Boolean> future = emailService.sendOTPMail("username@test.com", otp);
        boolean status = future.get();
        assertFalse(status);
    }
}
