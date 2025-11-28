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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private static final Logger logger = LogManager.getLogger(WebSecurityConfig.class);

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/api/users/upload-avatar").authenticated()
                        .requestMatchers("/api/users/logout").authenticated()
                        .requestMatchers("/api/users/{username}/security-questions").authenticated()
                        .requestMatchers("/api/users/{userId}/ban").hasRole("ADMIN")
                        .requestMatchers("/api/users/{userId}/unban").hasRole("ADMIN")
                        .requestMatchers("/api/users/{userId}/grant-admin").hasRole("ADMIN")
                        .requestMatchers("/api/users/{userId}/revoke-admin").hasRole("ADMIN")
                        .requestMatchers("/api/users/admin/reset-password/{userId}").hasRole("ADMIN")
                        .requestMatchers("/api/users/admin/security-question").hasRole("ADMIN")
                        .requestMatchers("/api/users/admin/security-question/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/profile").authenticated()
                        .requestMatchers("/api/users/admin/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/current").authenticated()
                        .requestMatchers("/api/users/current/feedback").authenticated()
                        .requestMatchers("/api/feedback/all").hasRole("ADMIN")
                        .requestMatchers("/api/feedback/type/**").hasRole("ADMIN")
                        .requestMatchers("/api/feedback/{id}/**").hasRole("ADMIN")
                        .requestMatchers("/api/forum/create").authenticated()
                        .requestMatchers("/api/forum/user/**").authenticated()
                        .requestMatchers("/api/forum/{forumId}").authenticated()
                        .requestMatchers("/api/forum/{forumId}/**").authenticated()
                        .requestMatchers("/api/points/save").authenticated()
                        .requestMatchers("/api/points/{pointId}/propose-delete").authenticated()
                        .requestMatchers("/api/points/{pointId}/restore").hasRole("ADMIN")
                        .requestMatchers("/api/points/{pointId}/confirm").hasRole("ADMIN")
                        .requestMatchers("/api/points/all").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
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
                        .authenticationEntryPoint(new Http403ForbiddenEntryPoint())
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .csrf(AbstractHttpConfigurer::disable)
                .headers((headers) -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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

    @Configuration
    public static class CorsConfig {
        @Bean
        public CorsFilter corsFilter() {
            return new CorsFilter(new UrlBasedCorsConfigurationSource());
        }
    }
}