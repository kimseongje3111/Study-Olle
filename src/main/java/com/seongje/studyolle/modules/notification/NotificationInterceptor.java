package com.seongje.studyolle.modules.notification;

import com.seongje.studyolle.modules.account.authentication.UserAccount;
import com.seongje.studyolle.modules.account.domain.Account;
import com.seongje.studyolle.modules.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class NotificationInterceptor implements HandlerInterceptor {

    private final NotificationRepository notificationRepository;

    // Handler Interceptor before view rendering //

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (modelAndView != null && !isRedirectView(modelAndView) && authentication != null
                && authentication.getPrincipal() instanceof UserAccount) {

            Account account = ((UserAccount) authentication.getPrincipal()).getAccount();
            long count = notificationRepository.countByAccountAndChecked(account, false);

            modelAndView.addObject("hasNotification", (count > 0));
        }
    }

    private boolean isRedirectView(ModelAndView modelAndView) {
        return Objects.requireNonNull(modelAndView.getViewName()).startsWith("redirect:")
                || modelAndView.getView() instanceof RedirectView;
    }
}
