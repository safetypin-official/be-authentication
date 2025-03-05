package com.safetypin.authentication.service;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.ServerSetup;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;


// Email integration test
@SpringBootTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
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
    void testSendOTPMail_Success() throws MessagingException, IOException {
        String otp = "123456";
        boolean status = emailService.sendOTPMail("username@test.com", otp);
        assertTrue(status);

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length);
        MimeMessage receivedMessage = receivedMessages[0];
        assertTrue(receivedMessage.getSubject().contains("OTP"));
        assertTrue(receivedMessage.getContent().toString().contains(otp));
    }

    @Test
    void testSendOTPMail_MailServerDown() {
        greenMail.stop();

        String otp = "123456";
        boolean status = emailService.sendOTPMail("username@test.com", otp);
        assertFalse(status);
    }
}
