package com.placement.placement_intelligence.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves the React SPA from Spring Boot's static resources.
 *
 * All non-API, non-static requests are forwarded to /index.html
 * so React Router can handle client-side navigation on full-page
 * loads (e.g., refreshing /dashboard, /login, etc.).
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve static assets (JS, CSS, images) from the classpath
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/");
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/favicon.svg")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/robots.txt")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/sitemap.xml")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // SPA fallback: any unknown path serves index.html
        // React Router then handles the route client-side
        registry.addViewController("/{path:[^\\.]*}")
                .setViewName("forward:/index.html");
        registry.addViewController("/{path:[^\\.]*}/**")
                .setViewName("forward:/index.html");
    }
}
