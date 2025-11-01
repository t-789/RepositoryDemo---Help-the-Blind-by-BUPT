package org.example.RepositoryDemo.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private static final Logger logger = LogManager.getLogger(WebSecurityConfig.class);
    
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/api/users/register").permitAll()
                        .requestMatchers("/api/users/login").permitAll()
                        .requestMatchers("/api/users/logout").authenticated()
                        .requestMatchers("/api/users/{userId}/ban").hasRole("ADMIN")
                        .requestMatchers("/api/users/{userId}/unban").hasRole("ADMIN")
                        .requestMatchers("/api/users/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/*/security-questions").permitAll()
                        .requestMatchers("/api/ai/**").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/release-notes").permitAll()
                        .requestMatchers("/curl.txt").permitAll()
                        .requestMatchers("/about").permitAll()
                        .requestMatchers("/index").permitAll()
                        .requestMatchers("/index*").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/error/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/feedback_management").hasRole("ADMIN")
                        .requestMatchers("/api/users/security-questions").permitAll()
                        .requestMatchers("/api/users/register-with-security").permitAll()
                        .requestMatchers("api/points/distance").permitAll()
                        .requestMatchers("/errortest").permitAll()
                        .requestMatchers("api/statistic/**").permitAll()
                        .requestMatchers("/").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureHandler(authenticationFailureHandler())
                        .permitAll()
                )
                .logout(logout->logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/error/403")
                )
                .csrf(AbstractHttpConfigurer::disable)
                .headers((headers) -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                );

        return http.build();
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }
    
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            String errorMessage = exception.getMessage();
            // 检查是否是账户被封禁的错误信息
            if (errorMessage.contains("账户被封禁至")) {
                // 提取封禁时间信息
                String bannedTime = errorMessage.substring(errorMessage.indexOf("账户被封禁至") + 6);
                // 重定向到登录页面并传递封禁时间信息
                response.sendRedirect("/login?banned=true&bannedTime=" + 
                    URLEncoder.encode(bannedTime, StandardCharsets.UTF_8));
            } else {
                // 其他错误情况，显示通用错误信息
                response.sendRedirect("/login?error=true");
            }
        };
    }
    
    // 自定义异常类，用于传递详细的封禁信息
    public static class DisabledException extends org.springframework.security.authentication.DisabledException {
        public DisabledException(String msg) {
            super(msg, null);
        }

    }
}