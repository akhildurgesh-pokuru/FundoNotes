package com.fundoonotes.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServiceMonitoringAspectTest {

    private ServiceMonitoringAspect aspect;

    private ProceedingJoinPoint joinPoint;

    @BeforeEach
    void setUp() {

        aspect = new ServiceMonitoringAspect();

        joinPoint = mock(ProceedingJoinPoint.class);

        // Provide a safe method name for the log.
        Signature signature = mock(Signature.class);

        when(signature.toShortString())
                .thenReturn(
                        "NoteServiceImpl.getAllNotes(..)"
                );

        when(joinPoint.getSignature())
                .thenReturn(signature);
    }

    @Test
    void monitorService_ShouldReturnOriginalResult()
            throws Throwable {

        Object originalResult = new Object();

        when(joinPoint.proceed())
                .thenReturn(originalResult);

        Object actualResult =
                aspect.monitorService(joinPoint);

        // AOP must preserve the original response.
        assertSame(originalResult, actualResult);

        // The underlying service executes only once.
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void monitorService_ShouldPropagateException()
            throws Throwable {

        IllegalStateException failure =
                new IllegalStateException("Test failure");

        when(joinPoint.proceed())
                .thenThrow(failure);

        IllegalStateException thrown =
                assertThrows(
                        IllegalStateException.class,
                        () -> aspect.monitorService(joinPoint)
                );

        assertSame(failure, thrown);

        verify(joinPoint, times(1)).proceed();
    }
}
