package com.ofss.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ofss.util.JwtFilter;

@Configuration
public class WebConfig {
    
    // Enable CORS for frontend (http://localhost:8000)
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        System.out.println("WebConfig - corsConfigurer method called - CORS enabled");
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")  // Allow CORS on all endpoints
                        .allowedOrigins("http://localhost:8000") // Your frontend origin
                        .allowedMethods("GET", "POST", "PUT", "DELETE") // Allowed HTTP methods
                        .allowCredentials(true);
            }
        };
    }
    
    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration(JwtFilter jwtFilter) {
        FilterRegistrationBean<JwtFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(jwtFilter);
        registrationBean.addUrlPatterns("/customers/*");
        return registrationBean;
    }

}
