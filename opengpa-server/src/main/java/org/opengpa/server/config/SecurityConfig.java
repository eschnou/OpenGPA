package org.opengpa.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import lombok.Data;
import org.opengpa.frontend.views.LoginView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    private final ApplicationConfig applicationConfig;

    public SecurityConfig(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(
                auth -> auth.requestMatchers(new AntPathRequestMatcher("/actuator/**"))
                .permitAll());

        super.configure(httpSecurity);
        setLoginView(httpSecurity, LoginView.class);
    }

    @Bean
    UserDetailsManager userDetailsManager(List<UserDetails> users) {
        return new InMemoryUserDetailsManager(users);
    }

    @Bean
    @ConditionalOnProperty(prefix = "opengpa.server.auth", name = "provider", havingValue = "memory", matchIfMissing = true)
    List<UserDetails> inMemoryUsers() {
        return Arrays.asList(User.withUsername(applicationConfig.getUsername())
                .password(String.format("{noop}%s", applicationConfig.getPassword()))
                .roles("USER").build());
    }

    @Bean
    @ConditionalOnProperty(prefix = "opengpa.server.auth", name = "provider", havingValue = "file", matchIfMissing = false)
    List<UserDetails> inFileUsers(@Value("${opengpa.server.auth.file}") String userDetailsFilePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<FileUser> users = objectMapper.readValue(new File(userDetailsFilePath), objectMapper.getTypeFactory().constructCollectionType(List.class, FileUser.class));
        List<UserDetails> userDetails = new ArrayList<>();

        for (FileUser user : users) {
            userDetails.add(new User(user.getUsername(), user.getPassword(), user.getRoles().stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList())));
        }

        return userDetails;
    }

    @Bean
    @ConditionalOnProperty(prefix = "opengpa.server.auth", name = "provider", havingValue = "file", matchIfMissing = false)
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Data
    private static class FileUser {
        private String username;
        private String password;
        private List<String> roles;
    }
}
