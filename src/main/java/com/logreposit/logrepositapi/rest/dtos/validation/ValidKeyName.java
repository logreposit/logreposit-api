package com.logreposit.logrepositapi.rest.dtos.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

// must begin with a-z
// can continue with 0-9, a-z and underscores _
// must end with 0-9 or a-z
// must not be "time" (reserved keyword)
@NotBlank
@Pattern(regexp = "^(?!^time$)([a-z]+[0-9a-z_]*)?[0-9a-z]+$")
// @Pattern(regexp = "^(?!^time$)[a-z]+[0-9a-z_]*[0-9a-z]+$")
@Target({FIELD, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface ValidKeyName {
  String message() default
      "Invalid name. Must match regex `^(?!^time$)[a-z]+[0-9a-z_]*[0-9a-z]+$`.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
