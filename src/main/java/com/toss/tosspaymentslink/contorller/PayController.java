package com.toss.tosspaymentslink.contorller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PayController {

    @GetMapping("/")
    public String home() {
        return "redirect:/home.html";
    }
}
