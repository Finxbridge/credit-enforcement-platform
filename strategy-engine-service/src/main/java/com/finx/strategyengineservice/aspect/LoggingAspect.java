package com.finx.strategyengineservice.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AOP Aspect for standardized logging across Controllers and Services
 * Logs: Controller Start -> Service Start -> Service End -> Controller End
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    // ==================== CONTROLLER LOGGING ====================

    @Pointcut("within(com.finx.strategyengineservice.controller..*)")
    public void controllerMethods() {}

    @Before("controllerMethods()")
    public void logControllerEntry(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String endpoint = getRequestInfo();

        log.info("[{}] API: {} - {} started", className, endpoint, methodName);
    }

    @AfterReturning("controllerMethods()")
    public void logControllerExit(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String endpoint = getRequestInfo();

        log.info("[{}] API: {} - {} completed", className, endpoint, methodName);
    }

    @AfterThrowing(pointcut = "controllerMethods()", throwing = "ex")
    public void logControllerException(JoinPoint joinPoint, Throwable ex) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String endpoint = getRequestInfo();

        log.error("[{}] API: {} - {} failed: {}", className, endpoint, methodName, ex.getMessage());
    }

    // ==================== SERVICE LOGGING ====================

    @Pointcut("within(com.finx.strategyengineservice.service.impl..*)")
    public void serviceMethods() {}

    @Before("serviceMethods()")
    public void logServiceEntry(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.info("[{}] Method started: {}", className, methodName);
    }

    @AfterReturning("serviceMethods()")
    public void logServiceExit(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.info("[{}] Method ended: {}", className, methodName);
    }

    @AfterThrowing(pointcut = "serviceMethods()", throwing = "ex")
    public void logServiceException(JoinPoint joinPoint, Throwable ex) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.error("[{}] Method failed: {} - {}", className, methodName, ex.getMessage());
    }

    // ==================== HELPER METHODS ====================

    private String getRequestInfo() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getMethod() + " " + request.getRequestURI();
            }
        } catch (Exception e) {
            // Ignore - might not be in web context
        }
        return "N/A";
    }
}
