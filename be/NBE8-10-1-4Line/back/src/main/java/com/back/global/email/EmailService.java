package com.back.global.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    // yml에 설정한 환경 변수를 가져옵니다.
    @Value("${custom.site.order-url}")
    private String orderUrl;

    @Async
    public void sendStatusEmail(String toEmail, String statusName) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            // HTML 메일을 보내기 위해 true(multipart) 설정
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setFrom("Cafe Management <ic366632@gmail.com>");
            helper.setSubject("[Cafe Management] 주문 상태 변경 안내");

            String koreanStatus = switch (statusName) {
                case "ORDERED" -> "주문 완료";
                case "PAID" -> "결제 완료";
                case "PREPARING" -> "배송 준비 중";
                case "SHIPPING" -> "배송 중";
                case "DELIVERED" -> "배송 완료";
                case "CANCELED" -> "주문 취소";
                default -> statusName;
            };

            String htmlContent = String.format(
                    "<h3>고객님의 주문 상태가 [%s]으로 변경되었습니다.</h3>" +
                            "<p>배송 상태 확인 >> <a href='%s'>Link</a> << </p>",
                    koreanStatus, orderUrl
            );

            helper.setText(htmlContent, true); // true 설정이 HTML임을 의미합니다.

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            // 에러 로깅 처리
            throw new RuntimeException("메일 발송 중 오류 발생", e);
        }
    }
}