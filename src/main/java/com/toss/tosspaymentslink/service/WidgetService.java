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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class WidgetService {

    @Value("${widget.secret.key}")
    private String WIDGET_SECRET_KEY;

    private final WidgetRepository widgetRepository;
    private final ProductRepository productRepository;
    private final RestClient restClient;

    public WidgetService(WidgetRepository widgetRepository, ProductRepository productRepository, RestClient restClient) {
        this.widgetRepository = widgetRepository;
        this.productRepository = productRepository;
        this.restClient = restClient;
    }

    @Transactional
    public JSONObject payment(PaymentConfirmRequestDto requestDto) {
        verifyProductInfo(requestDto);

        JSONObject requestObj = new JSONObject();
        requestObj.put("paymentKey", requestDto.paymentKey());
        requestObj.put("orderId", requestDto.orderId());
        requestObj.put("amount", requestDto.amount());
        requestObj.put("productId", requestDto.productId());
        requestObj.put("name", requestDto.name());

        String authorizations = "Basic " + Base64.getEncoder()
                .encodeToString((WIDGET_SECRET_KEY + ":").getBytes(StandardCharsets.UTF_8));

        try {
            JSONObject responseJson = restClient.post()
                    .uri("https://api.tosspayments.com/v1/payments/confirm")
                    .header("Authorization", authorizations)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestObj)
                    .retrieve()
                    .body(JSONObject.class);
            log.info("결제 승인 성공: {}", responseJson);

            Long productId = Long.valueOf(requestObj.get("productId").toString());
            Product product = productRepository.findByIdWithLock(productId)
                    .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));
            //영속성 컨텍스트(Persistence Context)
            //트랜잭션 종료 시 '변경 감지(Dirty Checking)' 동작, update 실행
            product.decreaseStock(1);

            // API 응답 객체를 반환하도록 수정 (기존엔 null 반환)
            saveResponse(responseJson);
            return responseJson;

        }catch (RestClientResponseException e) {
            // 토스 API에서 4xx, 5xx 에러 응답을 보낸 경우 (응답 Body 파싱)
            log.error("결제 승인 실패 - Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalArgumentException("결제 승인에 실패했습니다: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("결제 승인 중 예기치 않은 오류 발생", e);
            throw new RuntimeException("결제 처리 중 서버 에러가 발생했습니다.");
        }
    }

    //todo: 결제취소 만들기
    public JSONObject cancelPayment() {
        return new JSONObject();
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
        int totalAmount = ((Number) responseJson.get("totalAmount")).intValue();
        String method = (String) responseJson.get("method");
        PaymentStatus status = PaymentStatus.valueOf((String) responseJson.get("status"));
        String orderName = (String) responseJson.get("orderName");

        OffsetDateTime requestedAt = Optional.ofNullable((String) responseJson.get("requestedAt"))
                .map(OffsetDateTime::parse).orElse(null);
        OffsetDateTime approvedAt = Optional.ofNullable((String) responseJson.get("approvedAt"))
                .map(OffsetDateTime::parse).orElse(null);

        // 💡 Map으로 형변환하여 receipt.url 추출
        String receiptUrl = Optional.ofNullable((Map<?, ?>) responseJson.get("receipt"))
                .map(receipt -> (String) receipt.get("url"))
                .orElse(null);

        // 💡 Map으로 형변환하여 card 정보 추출
        String cardInfo = Optional.ofNullable((Map<?, ?>) responseJson.get("card"))
                .map(card -> String.format("%s_%s", card.get("ownerType"), card.get("number")))
                .orElse(null);

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

    private void verifyProductInfo(PaymentConfirmRequestDto requestDto) {
        Product findProduct = productRepository.findById(requestDto.productId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. ID: " + requestDto.productId()));

        if (!findProduct.getName().equals(requestDto.name())) {
            log.error("💥 상품명 불일치! DB: {}, 요청: {}", findProduct.getName(), requestDto.name());
            throw new IllegalArgumentException("상품 정보가 일치하지 않습니다.");
        }
        if (findProduct.getPrice() != requestDto.amount()) {
            log.error("결제 금액 불일치! DB: {}, 요청: {}", findProduct.getPrice(), requestDto.amount());
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }
        log.info("✅ 상품 검증 완료 - 상품명: {}, 금액: {}원", findProduct.getName(), findProduct.getPrice());
    }
}