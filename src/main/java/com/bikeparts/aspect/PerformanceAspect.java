package com.bikeparts.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Aspect
public class PerformanceAspect {
public static final Logger log = LoggerFactory.getLogger(PerformanceAspect.class);

    @Around("execution(* com.bikeparts.service.*.*(..))")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        String methodName = proceedingJoinPoint.getSignature().getName();
        String className = proceedingJoinPoint.getTarget().getClass().getName();
        long start = System.currentTimeMillis();
        Object result;
        try {
            result = proceedingJoinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("Duration of {}.{} in ms: {}", methodName, className, duration);
        } catch (Exception e) {
            // after the method in error case
            long duration = System.currentTimeMillis() - start;
            log.info("{}.{} failed after {}ms", className, methodName, duration);
            throw e;
        }
        return result;
    }
}
