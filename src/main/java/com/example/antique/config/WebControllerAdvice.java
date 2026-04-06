package com.example.antique.config;

import com.example.antique.service.AntiqueInspectionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class WebControllerAdvice {

    private final AntiqueInspectionService antiqueInspectionService;

    @ModelAttribute("currentUri")
    public String currentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    /**
     * Expose pending inspection count for the sidebar badge on every page.
     */
    @ModelAttribute("pendingInspectionsBadge")
    public long pendingInspectionsBadge() {
        try {
            return antiqueInspectionService.countPending();
        } catch (Exception e) {
            return 0;
        }
    }
}
