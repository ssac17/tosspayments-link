package com.toss.tosspaymentslink.contorller;

import com.toss.tosspaymentslink.dto.PaymentResponseDto;
import com.toss.tosspaymentslink.service.WidgetService;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Slf4j
@Controller
public class WidgetController {
    private final WidgetService widgetService;

    public WidgetController(WidgetService widgetService) {
        this.widgetService = widgetService;
    }

    @PostMapping(value = "/confirm")
    public ResponseEntity<JSONObject> confirm(@RequestBody String jsonBody){
        JSONObject jsonObject = widgetService.payment(jsonBody);
        return ResponseEntity.ok(jsonObject);
    }

    @GetMapping("/api/payments")
    public ResponseEntity<Page<PaymentResponseDto>> getPayments(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "approvedAt"));
        return ResponseEntity.ok(widgetService.getPayments(pageable));
    }
}