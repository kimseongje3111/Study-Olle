package com.seongje.studyolle.modules.main;

import com.seongje.studyolle.modules.account.authentication.CurrentUser;
import com.seongje.studyolle.domain.account.Account;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home(@CurrentUser Account account, Model model) {
        if (account != null) {
            model.addAttribute("account", account);
        }

        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "main/login";
    }

    @GetMapping("/email-login")
    public String emailLogin() {
        return "main/email-login";
    }
}
