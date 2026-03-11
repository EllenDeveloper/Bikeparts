package com.bikeparts.config;

import com.bikeparts.entity.Account;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.SessionScope;

@Configuration
public class AccountConfig {

    @Bean
    @SessionScope
    public Account account() {
        return new Account();
    }
}
