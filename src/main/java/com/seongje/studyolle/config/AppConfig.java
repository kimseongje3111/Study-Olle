package com.seongje.studyolle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {

        // Password Encoder //
        // 해싱 알고리즘 (bcrypt), 솔트 (Salt) //

        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
