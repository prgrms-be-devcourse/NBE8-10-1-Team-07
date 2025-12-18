package com.back.global.email;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Async
    public void sendStatusEmail(String toEmail, String statusName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom("Cafe Management <ic366632@gmail.com>"); //
        message.setSubject("[Cafe Management] 주문 상태 변경 안내");
        message.setText("고객님의 주문 상태가 [" + statusName + "]으로 변경되었습니다.");

        mailSender.send(message);
    }
}