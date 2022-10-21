package com.rhbgroup.dcpbo.customer.aspect;


import com.rhbgroup.dcp.model.Capsule;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CapsuleAspect {

    @Autowired
    ApiContext apiContext;


    @Around("@annotation(com.rhbgroup.dcpbo.customer.annotation.DcpIntegration)")
    public Object prepareCapsule(ProceedingJoinPoint joinPoint) throws Throwable {
        // add logic

        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();

        String username = (String) joinPoint.getArgs()[0];

        apiContext.setUser(username);

        apiContext.setCapsule(new Capsule());


        Object proceed = joinPoint.proceed();

        return proceed;
    }
}
