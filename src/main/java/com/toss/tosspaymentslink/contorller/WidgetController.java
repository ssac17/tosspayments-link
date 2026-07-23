package com.toss.tosspaymentslink.contorller;

import com.toss.tosspaymentslink.service.WidgetService;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@Slf4j
@Controller
public class WidgetController {
    private final WidgetService widgetService;

    public WidgetController(WidgetService widgetService) {
        this.widgetService = widgetService;
    }

    @RequestMapping(value = "/confirm")
    public ResponseEntity<JSONObject> confirm(@RequestBody String jsonBody){
        JSONObject jsonObject = widgetService.payment(jsonBody);
        return ResponseEntity.ok(jsonObject);
    }
}