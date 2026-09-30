package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import vn.iotstar.service.EmailService;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String sender;

    @Override
    public void sendOtp(String email, String otp, String subject) {
        if (sender == null || sender.isBlank()) {
            log.info("OTP DEMO -> email={}, otp={}, subject={}", email, otp, subject);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText("Mã OTP của bạn là: " + otp
                + "\nOTP có hiệu lực trong 5 phút và chỉ sử dụng một lần.");
        mailSender.send(message);
    }
}
