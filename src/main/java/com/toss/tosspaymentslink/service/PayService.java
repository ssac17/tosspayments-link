package com.toss.tosspaymentslink.service;

import com.toss.tosspaymentslink.domain.embeded.*;
import com.toss.tosspaymentslink.domain.enums.AcquireStatus;
import com.toss.tosspaymentslink.domain.enums.PayMethod;
import com.toss.tosspaymentslink.domain.enums.Type;
import com.toss.tosspaymentslink.dto.PageResponseDto;
import com.toss.tosspaymentslink.dto.PaymentConfirmRequestDto;
import com.toss.tosspaymentslink.domain.entity.Payment;
import com.toss.tosspaymentslink.domain.enums.PaymentStatus;
import com.toss.tosspaymentslink.domain.entity.Product;
import com.toss.tosspaymentslink.dto.PaymentResponseDto;
import com.toss.tosspaymentslink.repository.PayRepository;
import com.toss.tosspaymentslink.repository.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
public class PayService {

    @Value("${widget.secret.key}")
    private String WIDGET_SECRET_KEY;

    private final PayRepository payRepository;
    private final ProductRepository productRepository;
    private final RestClient restClient;

    public PayService(PayRepository payRepository, ProductRepository productRepository, RestClient restClient) {
        this.payRepository = payRepository;
        this.productRepository = productRepository;
        this.restClient = restClient;
    }

    @Transactional
    public PaymentResponseDto payment(PaymentConfirmRequestDto requestDto) {
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
            Payment savedPayment = saveResponse(responseJson);
            return PaymentResponseDto.from(savedPayment);

        }catch (RestClientResponseException e) {
            // 토스 API에서 4xx, 5xx 에러 응답을 보낸 경우 (응답 Body 파싱)
            log.error("결제 승인 실패 - Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalArgumentException("결제 승인에 실패했습니다: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("결제 승인 중 예기치 않은 오류 발생", e);
            throw new RuntimeException("결제 처리 중 서버 에러가 발생했습니다.");
        }
    }

    public PageResponseDto<PaymentResponseDto> getPayments(Pageable pageable) {
        Page<Payment> paymentPage = payRepository.findAll(pageable);
        Page<PaymentResponseDto> dtoPage = paymentPage.map(PaymentResponseDto::from);
        return PageResponseDto.from(dtoPage);
    }

    //todo: 결제취소 만들기

    public JSONObject cancelPayment() {
        return new JSONObject();
    }

    public Product cancelPayment(String jsonBody) {
        return null;
    }
    //public Page<PaymentResponseDto> getPayments(Pageable pageable) {
    //    Page<Payment> paymentPage = widgetRepository.findAll(pageable);
    //    return paymentPage.map(PaymentResponseDto::from);

    //}

    private Payment saveResponse(JSONObject responseJson) {
        // 응답 저장 로직
        Payment.PaymentBuilder builder = Payment.builder();
        builder.version((String) responseJson.get("version"))
                .paymentKey((String) responseJson.get("paymentKey"))
                .type(Type.valueOf(((String) responseJson.get("type")).toUpperCase()))
                .orderId((String) responseJson.get("orderId"))
                .orderName((String) responseJson.get("orderName"))
                .mId((String) responseJson.get("mId"))
                .currency((String) responseJson.get("currency"))
                .method(PayMethod.from(((String) responseJson.get("method")).toUpperCase()))
                .totalAmount(((Number) responseJson.get("totalAmount")).intValue())
                .balanceAmount(((Number) responseJson.get("balanceAmount")).intValue())
                .status(PaymentStatus.valueOf(((String) responseJson.get("status")).toUpperCase()))
                .requestedAt(OffsetDateTime.parse((String) responseJson.get("requestedAt")))
                .approvedAt(OffsetDateTime.parse((String) responseJson.get("approvedAt")))
                .useEscrow((Boolean) responseJson.get("useEscrow"))
                .lastTransactionKey((String) responseJson.get("lastTransactionKey"))
                .suppliedAmount(((Number) responseJson.get("suppliedAmount")).intValue())
                .vat(((Number) responseJson.get("vat")).intValue())
                .cultureExpense((Boolean) responseJson.get("cultureExpense"))
                .taxFreeAmount(((Number) responseJson.get("taxFreeAmount")).intValue())
                .taxExemptionAmount(((Number) responseJson.get("taxExemptionAmount")).intValue())
                .isPartialCancelable((Boolean) responseJson.get("isPartialCancelable"))
                .metadata(String.valueOf(responseJson.get("metadata")))
                .receiptUrl((String) responseJson.get("receiptUrl"))
                .checkoutUrl((String) responseJson.get("checkoutUrl"))
                .country((String) responseJson.get("country"))
                .discountAmount(((Integer) responseJson.get("discountAmount")));

        //카드결제 시
        if(responseJson.get("card") != null) {
            Map<String, Object> cardMap = (Map<String, Object>) responseJson.get("card");
            Card card = Card.builder()
                    .amount(((Integer) cardMap.get("amount")))
                    .issuerCode((String) cardMap.get("issuerCode"))
                    .acquirerCode((String) cardMap.get("acquirerCode"))
                    .number((String) cardMap.get("number"))
                    .installmentPlanMonths(((Integer) cardMap.get("installmentPlanMonths")))
                    .approveNo((String) cardMap.get("approveNo"))
                    .useCardPoint((Boolean) cardMap.get("useCardPoint"))
                    .cardType((String) cardMap.get("cardType"))
                    .ownerType((String) cardMap.get("ownerType"))
                    .acquireStatus(cardMap.get("acquireStatus") != null ? AcquireStatus.valueOf((String) cardMap.get("acquireStatus")) : null)
                    .isInterestFree((Boolean) cardMap.get("isInterestFree"))
                    .interestPayer((String) cardMap.get("interestPayer"))
                    .build();
            builder.card(card);
        }
        //계좌 이체 시
        if(responseJson.get("transfer") != null) {
            Map<String, Object> transferMap = (Map<String, Object>) responseJson.get("transfer");
            Transfer transfer = Transfer.builder()
                    .bankCode((String) transferMap.get("bankCode"))
                    .settlementStatus((String) transferMap.get("settlementStatus"))
                    .build();
            builder.transfer(transfer);
        }
        //간편 결제 시
        if(responseJson.get("easyPay") != null) {
            Map<String, Object> paymentMap = (Map<String, Object>) responseJson.get("easyPay");
            EasyPay easyPay = EasyPay.builder()
                    .provider((String) paymentMap.get("provider"))
                    .amount(((Integer) paymentMap.get("amount")))
                    .discountAmount(((Integer) paymentMap.get("discountAmount")))
                    .build();
            builder.easyPay(easyPay);
        }

        //현금 영수증
        if(responseJson.get("cashReceipt") != null) {
            Map<String, Object> cashReceiptMap = (Map<String, Object>) responseJson.get("cashReceipt");
            CashReceipt cashReceipt = CashReceipt.builder()
                    .type((String) cashReceiptMap.get("type"))
                    .receiptKey((String) cashReceiptMap.get("receiptKey"))
                    .issueNumber((String) cashReceiptMap.get("issueNumber"))
                    .receiptUrl((String) cashReceiptMap.get("receiptUrl"))
                    .amount(((Integer) cashReceiptMap.get("amount")))
                    .taxFreeAmount(((Integer) cashReceiptMap.get("taxFreeAmount")))
                    .build();
            builder.cashReceipt(cashReceipt);
        }
        Payment newPayment = builder.build();
        payRepository.save(newPayment);
        return newPayment;
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