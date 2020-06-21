package com.seongje.studyolle.modules.main;

import com.seongje.studyolle.modules.account.authentication.CurrentUser;
import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.study.domain.Study;
import com.seongje.studyolle.modules.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final StudyRepository studyRepository;

    @GetMapping("/")
    public String home(@CurrentUser Account account, Model model) {
        if (account != null) {
            model.addAttribute("account", account);

            return "index-login";
        }

        List<Study> mostRecent6Studies =
                studyRepository.findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(true, false);

        model.addAttribute("mostRecent6Studies", mostRecent6Studies);

        return "index";
    }
}
