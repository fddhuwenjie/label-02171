package com.medcommunity.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class OperationLogAspect {

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void operationPointcut() {
    }

    @Around("operationPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        String method = "";
        String uri = "";
        String username = "";
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            method = request.getMethod();
            uri = request.getRequestURI();
            Object usernameAttr = request.getAttribute("username");
            username = usernameAttr != null ? usernameAttr.toString() : "anonymous";
        }

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String args = Arrays.toString(joinPoint.getArgs());

        log.info("操作日志 - 用户: {}, 请求: {} {}, 方法: {}.{}, 参数: {}",
                username, method, uri, className, methodName, args);

        Object result = joinPoint.proceed();

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("操作日志 - 方法: {}.{}, 耗时: {}ms", className, methodName, elapsed);

        return result;
    }
}
