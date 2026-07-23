package com.toss.tosspaymentslink.contorller;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
public class WidgetController {

    @RequestMapping(value = "/confirm")
    public ResponseEntity<JSONObject> confirmPayment(@RequestBody String jsonBody) throws Exception {
        log.info("WidgetController.confirmPayment");
        log.info("jsonBody: " + jsonBody);
        return ResponseEntity.ok(new JSONObject());
    }
}