package com.logreposit.logrepositapi.rest.dtos.validation;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class ValidKeyNameTests {
  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  public void testValidKeyName_givenValidName_expectSuccess() {
    SomeTestClass testObject = new SomeTestClass("some_name");

    Set<ConstraintViolation<SomeTestClass>> violations = this.validator.validate(testObject);

    assertThat(violations).isEmpty();
  }

  @Test
  public void testValidKeyName_givenInvalidNameStartingWithNumber_expectValidationError() {
    SomeTestClass testObject = new SomeTestClass("12some_name");

    Set<ConstraintViolation<SomeTestClass>> violations = this.validator.validate(testObject);

    assertThat(violations).hasSize(1);

    ConstraintViolation<SomeTestClass> violation = violations.iterator().next();

    assertThat(violation).isNotNull();
    assertThat(violation.getMessage())
        .isEqualTo("must match \"^(?!^time$)[a-z]+[0-9a-z_]*[0-9a-z]+$\"");
    assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
    assertThat(violation.getInvalidValue()).isEqualTo("12some_name");
  }

  @Test
  public void testValidKeyName_givenInvalidStringInSet_expectValidationError() {
    SomeTestClass testObject =
        new SomeTestClass(
            "valid_name", "my_tag_1", "my_quite_long_tag", "_invalid_tag", "another_valid_tag");

    Set<ConstraintViolation<SomeTestClass>> violations = this.validator.validate(testObject);

    assertThat(violations).hasSize(1);

    ConstraintViolation<SomeTestClass> violation = violations.iterator().next();

    assertThat(violation).isNotNull();
    assertThat(violation.getMessage())
        .isEqualTo("must match \"^(?!^time$)[a-z]+[0-9a-z_]*[0-9a-z]+$\"");
    assertThat(violation.getPropertyPath().toString()).isEqualTo("tags[].<iterable element>");
    assertThat(violation.getInvalidValue()).isEqualTo("_invalid_tag");
  }

  @Test
  public void testValidKeyName_givenInvalidStringTime_expectValidationError() {
    SomeTestClass testObject =
        new SomeTestClass("time", "my_tag_1", "my_quite_long_tag", "time", "another_valid_tag");

    Set<ConstraintViolation<SomeTestClass>> violations = this.validator.validate(testObject);

    assertThat(violations).hasSize(2);

    ConstraintViolation<SomeTestClass> tagError =
        violations.stream()
            .filter(v -> "tags[].<iterable element>".equals(v.getPropertyPath().toString()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("should not be here"));
    ConstraintViolation<SomeTestClass> fieldNameError =
        violations.stream()
            .filter(v -> "name".equals(v.getPropertyPath().toString()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("should not be here"));

    assertThat(tagError).isNotNull();
    assertThat(tagError.getMessage())
        .isEqualTo("must match \"^(?!^time$)[a-z]+[0-9a-z_]*[0-9a-z]+$\"");
    assertThat(tagError.getPropertyPath().toString()).isEqualTo("tags[].<iterable element>");
    assertThat(tagError.getInvalidValue()).isEqualTo("time");

    assertThat(fieldNameError).isNotNull();
    assertThat(fieldNameError.getMessage())
        .isEqualTo("must match \"^(?!^time$)[a-z]+[0-9a-z_]*[0-9a-z]+$\"");
    assertThat(fieldNameError.getPropertyPath().toString()).isEqualTo("name");
    assertThat(fieldNameError.getInvalidValue()).isEqualTo("time");
  }

  @Test
  public void testValidKeyName_givenEmptyName_expectValidationError() {
    SomeTestClass testObject = new SomeTestClass("");

    Set<ConstraintViolation<SomeTestClass>> violations = this.validator.validate(testObject);

    assertThat(violations).hasSize(2);

    ConstraintViolation<SomeTestClass> notBlankError =
        violations.stream()
            .filter(
                v ->
                    "{jakarta.validation.constraints.NotBlank.message}"
                        .equals(v.getMessageTemplate()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("should not be here"));
    ConstraintViolation<SomeTestClass> keyNameError =
        violations.stream()
            .filter(
                v ->
                    "{jakarta.validation.constraints.Pattern.message}"
                        .equals(v.getMessageTemplate()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("should not be here"));

    assertThat(notBlankError).isNotNull();
    assertThat(notBlankError.getMessage()).isEqualTo("must not be blank");
    assertThat(notBlankError.getPropertyPath().toString()).isEqualTo("name");
    assertThat(notBlankError.getInvalidValue()).isEqualTo("");

    assertThat(keyNameError).isNotNull();
    assertThat(keyNameError.getMessage())
        .isEqualTo("must match \"^(?!^time$)[a-z]+[0-9a-z_]*[0-9a-z]+$\"");
    assertThat(keyNameError.getPropertyPath().toString()).isEqualTo("name");
    assertThat(keyNameError.getInvalidValue()).isEqualTo("");
  }

  private static class SomeTestClass {
    @ValidKeyName private final String name;

    private final Set<@ValidKeyName String> tags;

    SomeTestClass(String name) {
      this.name = name;
      this.tags = new HashSet<>();
    }

    SomeTestClass(String name, String... tagNames) {
      this.name = name;
      this.tags = new HashSet<>(Arrays.asList(tagNames));
    }

    public String getName() {
      return this.name;
    }

    public Set<String> getTags() {
      return this.tags;
    }
  }
}
