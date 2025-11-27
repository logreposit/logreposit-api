package com.logreposit.logrepositapi.configuration.conditional;

import com.logreposit.logrepositapi.configuration.ApplicationConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Conditional;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnEnabledApplicationModeCondition.class)
public @interface ConditionalOnEnabledApplicationMode {
  ApplicationConfiguration.ApplicationMode mode();
}
