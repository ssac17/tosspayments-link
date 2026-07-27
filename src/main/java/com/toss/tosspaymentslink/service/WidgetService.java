package com.toss.tosspaymentslink.service;

import com.toss.tosspaymentslink.dto.PaymentResponseDto;
import com.toss.tosspaymentslink.entity.Payment;
import com.toss.tosspaymentslink.entity.PaymentStatus;
import com.toss.tosspaymentslink.repository.WidgetRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class WidgetService {

    @Value("${widget.secret.key}")
    private String WIDGET_SECRET_KEY;

    private final WidgetRepository widgetRepository;

    public WidgetService(WidgetRepository widgetRepository) {
        this.widgetRepository = widgetRepository;
    }

    public JSONObject payment(String jsonBody) {
        JSONObject requestObj = jsonParseObject(jsonBody);
        if (requestObj == null) {
            log.error("요청 JSON 파싱에 실패했습니다.");
            return null;
        }

        String authorizations = "Basic " + Base64.getEncoder()
                .encodeToString((WIDGET_SECRET_KEY + ":").getBytes(StandardCharsets.UTF_8));

        try {
            URL url = new URL("https://api.tosspayments.com/v1/payments/confirm");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", authorizations);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // 1. try-with-resources를 통한 안전한 OutputStream 자원 해제
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(requestObj.toString().getBytes(StandardCharsets.UTF_8));
            }

            int code = connection.getResponseCode();
            boolean isSuccess = code == 200;

            // 2. try-with-resources를 통한 안전한 InputStream 및 Reader 자원 해제
            try (InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();
                 Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {

                JSONParser parser = new JSONParser();
                JSONObject responseJson = (JSONObject) parser.parse(reader);

                if (isSuccess) {
                    log.info("결제 승인 성공: {}", responseJson);
                } else {
                    log.warn("결제 승인 실패: {}", responseJson);
                }
                // API 응답 객체를 반환하도록 수정 (기존엔 null 반환)
                saveResponse(responseJson);
                return responseJson;
            }

        } catch (IOException | ParseException e) {
            log.error("결제 승인 API 호출 중 오류 발생", e);
        }
        return null;
    }

    public Page<PaymentResponseDto> getPayments(Pageable pageable) {
        Page<Payment> paymentPage = widgetRepository.findAll(pageable);
        return paymentPage.map(PaymentResponseDto::from);
    }

    private JSONObject jsonParseObject(String jsonBody) {
        log.info("jsonBody: {}", jsonBody);
        JSONParser jsonParser = new JSONParser();

        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonBody);

            // 토스 결제 승인 API에 꼭 필요한 3가지 데이터만 추출
            JSONObject obj = new JSONObject();
            obj.put("paymentKey", jsonObject.get("paymentKey"));
            obj.put("orderId", jsonObject.get("orderId"));
            obj.put("amount", jsonObject.get("amount"));

            return obj; // 기존 jsonObject 대신 정제된 obj 반환

        } catch (ParseException e) {
            log.error("JSON 파싱 중 오류 발생", e);
            return null; // 오류 시 명시적으로 null 반환
        }
    }

    private void saveResponse(JSONObject responseJson) {
        String orderId = responseJson.get("orderId").toString();
        String paymentKey = responseJson.get("paymentKey").toString();
        int totalAmount = ((Number)responseJson.get("totalAmount")).intValue();
        String method = (String) responseJson.get("method");
        PaymentStatus status = PaymentStatus.valueOf((String) responseJson.get("status"));
        String orderName =  (String) responseJson.get("orderName");
        OffsetDateTime requestedAt = Optional.ofNullable((String)responseJson.get("requestedAt")).map(OffsetDateTime::parse).orElse(null);
        OffsetDateTime approvedAt = Optional.ofNullable((String) responseJson.get("approvedAt")).map(OffsetDateTime::parse).orElse(null);
        String receiptUrl = (String) Optional.ofNullable((JSONObject)responseJson.get("receipt")).map(receipt -> receipt.get("url")).orElse(null);
        String cardInfo = Optional.ofNullable((JSONObject) responseJson.get("card")).map(card -> String.format("%s_%s", card.get("ownerType"), card.get("number"))).orElse(null);

        widgetRepository.save(Payment.builder()
                .orderId(orderId)
                .paymentKey(paymentKey)
                .totalAmount(totalAmount)
                .method(method)
                .status(status)
                .orderName(orderName)
                .requestedAt(requestedAt)
                .approvedAt(approvedAt)
                .receiptUrl(receiptUrl)
                .cardInfo(cardInfo)
                .build());
    }
}