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
        message.setFrom("Cafe Management <ic366632@gmail.com>");
        message.setSubject("[Cafe Management] 주문 상태 변경 안내");

        String koreanStatus = switch (statusName) {
            case "ORDERED" -> "주문 접수";
            case "PAID" -> "결제 완료";
            case "PREPARING" -> "배송 준비 중";
            case "SHIPPING" -> "배송 중";
            case "DELIVERED" -> "배송 완료";
            case "CANCELED" -> "주문 취소";
            default -> statusName; // 일치하는 게 없으면 원래 값 출력
        };

        String content = "고객님의 주문 상태가 [" + koreanStatus + "]으로 변경되었습니다.\n\n" +
                "배송 상태 확인 >> http://localhost:3000/orders/search <<";

        message.setText(content);


        mailSender.send(message);
    }
}