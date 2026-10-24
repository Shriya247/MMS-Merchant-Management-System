
package com.mms;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    // DISPLAY ADMIN LOGIN PAGE
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
