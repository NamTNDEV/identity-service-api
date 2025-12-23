package com.namudev.identity_service.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignDebugInterceptor {

//    @Bean
//    public RequestInterceptor logRequestInterceptor() {
//        return (RequestTemplate template) -> {
//            System.out.println("====== FEIGN REQUEST ======");
//            System.out.println("URL: " + template.url());
//            System.out.println("Method: " + template.method());
//            System.out.println("Headers: " + template.headers());
//            System.out.println("Body: " +
//                    (template.body() != null ? new String(template.body()) : "null"));
//            System.out.println("===========================");
//        };
//    }
}