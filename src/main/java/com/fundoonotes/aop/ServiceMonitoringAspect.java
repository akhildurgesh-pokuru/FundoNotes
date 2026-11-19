package com.fundoonotes.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceMonitoringAspect {

    private static final Logger log =
            LoggerFactory.getLogger(
                    ServiceMonitoringAspect.class
            );

    // UC13: Select methods in our existing service
    // implementation package and its subpackages.
    @Pointcut(
            "execution(public * com.fundoonotes.service.impl..*.*(..))"
    )
    public void serviceImplementationMethods() {
    }

    // Also monitor the UC10/UC12 notification service,
    // which lives directly inside the service package.
    @Pointcut(
            "execution(public * " +
                    "com.fundoonotes.service.ReminderNotificationService.*(..))"
    )
    public void reminderNotificationMethods() {
    }

    // Run around matching service method calls.
    @Around(
            "serviceImplementationMethods() || " +
                    "reminderNotificationMethods()"
    )
    public Object monitorService(
            ProceedingJoinPoint joinPoint
    ) throws Throwable {

        // Safe operation name, without method arguments.
        String operation = joinPoint
                .getSignature()
                .toShortString();

        long startedAt = System.nanoTime();

        boolean successful = false;

        try {
            // Execute the actual service method exactly once.
            Object result = joinPoint.proceed();

            successful = true;

            // Return the original result, unchanged.
            return result;

        } catch (Throwable ex) {

            // Log the exception type, not its message,
            // arguments, passwords, tokens, or payload.
            log.warn(
                    "UC13: Service operation failed: {} ({})",
                    operation,
                    ex.getClass().getSimpleName()
            );

            // Never swallow the error or change its behavior.
            throw ex;

        } finally {

            double durationMs =
                    (System.nanoTime() - startedAt)
                            / 1_000_000.0;

            log.info(
                    "UC13: Service={} | Status={} | Duration={} ms",
                    operation,
                    successful ? "SUCCESS" : "FAILED",
                    String.format(
                            java.util.Locale.ROOT,
                            "%.2f",
                            durationMs
                    )
            );
        }
    }
}
