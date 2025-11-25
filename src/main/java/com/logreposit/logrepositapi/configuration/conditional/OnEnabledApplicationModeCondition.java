package com.logreposit.logrepositapi.configuration.conditional;

import com.logreposit.logrepositapi.configuration.ApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnEnabledApplicationModeCondition extends SpringBootCondition {
  private static final Logger logger =
      LoggerFactory.getLogger(OnEnabledApplicationModeCondition.class);

  private static final String CONFIG_PREFIX = "app.modes";

  @Override
  public ConditionOutcome getMatchOutcome(
      ConditionContext context, AnnotatedTypeMetadata metadata) {
    final var applicationMode = extractApplicationMode(metadata);

    // ConfigProperties are not initialized at this point
    // bind manually to the subset we're interested in
    final var configuration =
        Binder.get(context.getEnvironment())
            .bind(CONFIG_PREFIX, ApplicationConfiguration.ApplicationModeConfiguration.class)
            .get();

    if (configuration.getEnabled().contains(applicationMode)) {
      logger.info("Application mode {} is enabled", applicationMode);

      return ConditionOutcome.match();
    }

    logger.info("Application mode {} is not enabled", applicationMode);

    return ConditionOutcome.noMatch(
        String.format(
            "Application mode %s is not enabled in %s.enabled", applicationMode, CONFIG_PREFIX));
  }

  private ApplicationConfiguration.ApplicationMode extractApplicationMode(
      AnnotatedTypeMetadata metadata) {
    final var attributes =
        metadata.getAnnotationAttributes(ConditionalOnEnabledApplicationMode.class.getName());

    if (attributes == null) {
      // TODO DoM
      throw new IllegalStateException("TODO DoM");
    }

    return (ApplicationConfiguration.ApplicationMode) attributes.get("mode");
  }
}
