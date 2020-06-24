package com.seongje.studyolle.modules.main;

import com.seongje.studyolle.modules.account.authentication.CurrentUser;
import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.account.service.AccountService;
import com.seongje.studyolle.modules.event.service.EventService;
import com.seongje.studyolle.modules.study.service.StudyService;
import com.seongje.studyolle.modules.tag.domain.Tag;
import com.seongje.studyolle.modules.zone.domain.Zone;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final AccountService accountService;
    private final StudyService studyService;
    private final EventService eventService;

    @GetMapping("/")
    public String home(@CurrentUser Account account, Model model) {
        if (account != null) {
            Account findAccount = accountService.findByEmail(account.getEmail());
            Set<Tag> userTags = accountService.getUserTags(account);
            Set<Zone> userZones = accountService.getUserZones(account);

            model.addAttribute(findAccount);
            model.addAttribute("userTags", userTags);
            model.addAttribute("userZones", userZones);
            model.addAttribute("userStudies", studyService.getUserStudiesForNotClosed(findAccount));
            model.addAttribute("recommendedStudies", studyService.getRecommendedStudiesForTagsAndZones(userTags, userZones));
            model.addAttribute("eventsToAttend", eventService.getEventsToAttend(findAccount));
            model.addAttribute("eventsForAttended", eventService.getEventsForAttended(findAccount));

            return "index-login";
        }

        model.addAttribute("mostRecent9Studies", studyService.getStudiesForRecent9());

        return "index";
    }
}
