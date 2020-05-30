package com.seongje.studyolle.infra.config;

import com.seongje.studyolle.modules.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.header.writers.frameoptions.WhiteListedAllowFromStrategy;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AccountService accountService;
    private final DataSource dataSource;

    // Custom Spring Security Configuration //

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // Request authority //

        http.authorizeRequests()
                .mvcMatchers("/", "/account/login", "/account/sign-up", "/account/check-email-token",
                        "/account/email-login", "/account/email-login-confirm").permitAll()
                .mvcMatchers(HttpMethod.GET, "/account/profile/*").permitAll()
                .anyRequest().authenticated();

        // Custom login & logout page //

        http.formLogin().loginPage("/account/login").permitAll();

        http.logout().logoutSuccessUrl("/");

        // Keep login //

        http.rememberMe()
                .userDetailsService(accountService)      // User details service
                .tokenRepository(tokenRepository());     // Token in DB
    }

    @Override
    public void configure(WebSecurity web) throws Exception {

        // Resources //

        web.ignoring()
                .mvcMatchers("/node_modules/**")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);

        return jdbcTokenRepository;
    }
}
