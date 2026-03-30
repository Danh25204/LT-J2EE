package com.example.antique.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Thymeleaf 3.1 (Spring Boot 3.x) removed the implicit #httpServletRequest
 * expression object. This advice exposes the current request URI as
 * a model attribute "currentUri" available in every template.
 *
 * Usage in templates: ${currentUri.startsWith('/dashboard')}
 */
@ControllerAdvice
public class WebControllerAdvice {

    @ModelAttribute("currentUri")
    public String currentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
