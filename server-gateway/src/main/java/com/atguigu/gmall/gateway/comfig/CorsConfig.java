package com.atguigu.gmall.gateway.comfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * projectName: GmallTwicePrastice
 * 吾常终日而思矣 不如须臾之所学!
 *
 * @author: Clarence
 * time: 2023/2/1 15:33 周三
 * description: 允许跨域对象
 */

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter(){
//      跨域配置对象
        CorsConfiguration corsConfiguration = new CorsConfiguration();
//        设置请求源
        corsConfiguration.addAllowedOrigin("*");
//        设置请求头
        corsConfiguration.addAllowedHeader("*");
//        设置请求方法
        corsConfiguration.addAllowedMethod("*");
//        设置请求允许携带COOKIE
        corsConfiguration.setAllowCredentials(true);
//        配置源对象
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**",corsConfiguration);

        return new CorsWebFilter(urlBasedCorsConfigurationSource);
    }



}
