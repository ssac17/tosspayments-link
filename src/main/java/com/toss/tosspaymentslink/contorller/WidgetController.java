package com.toss.tosspaymentslink.contorller;

import com.toss.tosspaymentslink.dto.PaymentResponseDto;
import com.toss.tosspaymentslink.service.WidgetService;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
    public ResponseEntity<List<PaymentResponseDto>> getPayments(){
        return ResponseEntity.ok(widgetService.getPayments());
    }

}