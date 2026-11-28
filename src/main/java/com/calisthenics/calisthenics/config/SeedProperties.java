package com.calisthenics.calisthenics.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.seed")
@Getter
@Setter
public class SeedProperties {

    private boolean enabled = false;
    private Account admin = new Account();
    private Account coach = new Account();
    private Account client = new Account();

    @Getter
    @Setter
    public static class Account {
        private String username;
        private String email;
        private String password;
        private String name;
        private String lastName;
        private String phone;
    }
}
