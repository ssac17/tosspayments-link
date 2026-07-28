package com.toss.tosspaymentslink.service;

import com.toss.tosspaymentslink.dto.PaymentConfirmRequestDto;
import com.toss.tosspaymentslink.dto.PaymentResponseDto;
import com.toss.tosspaymentslink.entity.Payment;
import com.toss.tosspaymentslink.entity.PaymentStatus;
import com.toss.tosspaymentslink.entity.Product;
import com.toss.tosspaymentslink.repository.ProductRepository;
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
public class WidgetService {

    @Value("${widget.secret.key}")
    private String WIDGET_SECRET_KEY;

    private final WidgetRepository widgetRepository;
    private final ProductRepository productRepository;

    public WidgetService(WidgetRepository widgetRepository, ProductRepository productRepository) {
        this.widgetRepository = widgetRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public JSONObject payment(PaymentConfirmRequestDto requestDto) {
        JSONObject requestObj = new JSONObject();
        requestObj.put("paymentKey", requestDto.paymentKey());
        requestObj.put("orderId", requestDto.orderId());
        requestObj.put("amount", requestDto.amount());
        requestObj.put("productId", requestDto.productId());
        requestObj.put("name", requestDto.name());

        if (requestObj == null) {
            log.error("요청 JSON 파싱에 실패했습니다.");
            throw new IllegalArgumentException("잘못된 요청 형식입니다.");
        }
        log.info("requestObj: {}", requestObj);

        boolean isValidProduct = verifyProductInfo(requestObj);
        if(!isValidProduct){
            JSONObject failResponse = new JSONObject();
            failResponse.put("code", "INVALID_PRODUCT");
            failResponse.put("message", "상품 정보 또는 결제 금액이 일치하지 않습니다.");
            return failResponse;
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

                    Long productId = Long.valueOf(requestObj.get("productId").toString());
                    Product product = productRepository.findByIdWithLock(productId)
                            .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));
                    //영속성 컨텍스트(Persistence Context)
                    //트랜잭션 종료 시 '변경 감지(Dirty Checking)' 동작, update 실행
                    product.decreaseStock(1);

                    // API 응답 객체를 반환하도록 수정 (기존엔 null 반환)
                    saveResponse(responseJson);
                } else {
                    log.warn("결제 승인 실패: {}", responseJson);
                }
                return responseJson;
            }

        } catch (IOException | ParseException e) {
            log.error("결제 승인 API 호출 중 오류 발생", e);
        }
        return null;
    }

    public Product cancelPayment(String jsonBody) {
        return null;
    }

    public Page<PaymentResponseDto> getPayments(Pageable pageable) {
        Page<Payment> paymentPage = widgetRepository.findAll(pageable);
        return paymentPage.map(PaymentResponseDto::from);
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

    private boolean verifyProductInfo(JSONObject requestObj) {
        Long productId = Long.parseLong(requestObj.get("productId").toString());
        String productName = requestObj.get("name").toString();
        long amount = Long.parseLong(requestObj.get("amount").toString());
        Product findProduct = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 상품입니다." + productId));

        if(!findProduct.getName().equals(productName)) {
            log.error("💥 상품명 불일치! DB: {}, 요청: {}", findProduct.getName(), productName);
            throw new IllegalArgumentException("상품 정보가 일치하지 않습니다.");
        }
        if (findProduct.getPrice() != amount) {
            log.error("결제 금액이 맞지 않습니다! DB: {}, 요청: {}", findProduct.getPrice(), amount);
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }
        log.info("✅ 상품 검증 완료 - 상품명: {}, 금액: {}원", findProduct.getName(), findProduct.getPrice());
        return true;
    }
}