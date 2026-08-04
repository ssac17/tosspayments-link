package com.toss.tosspaymentslink.contorller;

import com.toss.tosspaymentslink.dto.PageResponseDto;
import com.toss.tosspaymentslink.dto.PaymentConfirmRequestDto;
import com.toss.tosspaymentslink.dto.PaymentResponseDto;
import com.toss.tosspaymentslink.service.PayService;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@Controller
public class PayController {
    private final PayService payService;

    public PayController(PayService payService) {
        this.payService = payService;
    }

    @PostMapping("/v1/payments/confirm")
    public ResponseEntity<PaymentResponseDto> confirm(@RequestBody PaymentConfirmRequestDto requestDto) {
        PaymentResponseDto responseDto = payService.payment(requestDto);
        log.info("responseDto: {}", responseDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/v1/payments")
    public ResponseEntity<PageResponseDto<PaymentResponseDto>> getPayments(@RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "approvedAt"));
        return ResponseEntity.ok(payService.getPayments(pageable));
    }

    @GetMapping("/v1/payments/{paymentKey}")
    public ResponseEntity<PaymentResponseDto> getPaymentByPaymentKey(@PathVariable String paymentKey) {
        return payService.getPaymentByPaymentKey(paymentKey)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/v1/payments/orders/{orderId}")
    public ResponseEntity<PaymentResponseDto> getPaymentByOrderId(@PathVariable String orderId) {
        return payService.getPaymentByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

