package com.rhbgroup.dcpbo.customer.audit.collector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BoControllerAudit {

    String value() default "";
    String eventCode();
}
