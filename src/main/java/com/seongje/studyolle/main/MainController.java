package com.seongje.studyolle.main;

import com.seongje.studyolle.account.CurrentUser;
import com.seongje.studyolle.domain.Account;
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
}
