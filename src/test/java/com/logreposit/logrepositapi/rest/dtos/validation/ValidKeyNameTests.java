package com.logreposit.logrepositapi.rest.dtos.validation;

import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidKeyNameTests
{
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
        assertThat(violation.getMessage()).isEqualTo("must match \"^(?!^time$)[a-z]+[0-9a-z_]*[0-9a-z]+$\"");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
        assertThat(violation.getInvalidValue()).isEqualTo("12some_name");
    }

    @Test
    public void testValidKeyName_givenInvalidStringInSet_expectValidationError() {
        SomeTestClass testObject = new SomeTestClass("valid_name", "my_tag_1", "my_quite_long_tag", "_invalid_tag", "another_valid_tag");

        Set<ConstraintViolation<SomeTestClass>> violations = this.validator.validate(testObject);

        assertThat(violations).hasSize(1);

        ConstraintViolation<SomeTestClass> violation = violations.iterator().next();

        assertThat(violation).isNotNull();
        assertThat(violation.getMessage()).isEqualTo("must match \"^(?!^time$)[a-z]+[0-9a-z_]*[0-9a-z]+$\"");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("tags[].<iterable element>");
        assertThat(violation.getInvalidValue()).isEqualTo("_invalid_tag");
    }

    @Test
    public void testValidKeyName_givenInvalidStringTime_expectValidationError() {
        SomeTestClass testObject = new SomeTestClass("time", "my_tag_1", "my_quite_long_tag", "time", "another_valid_tag");

        Set<ConstraintViolation<SomeTestClass>> violations = this.validator.validate(testObject);

        assertThat(violations).hasSize(2);

        Iterator<ConstraintViolation<SomeTestClass>> iterator = violations.iterator();

        ConstraintViolation<SomeTestClass> firstError = iterator.next();
        ConstraintViolation<SomeTestClass> secondError = iterator.next();

        assertThat(firstError).isNotNull();
        assertThat(firstError.getMessage()).isEqualTo("must match \"^(?!^time$)[a-z]+[0-9a-z_]*[0-9a-z]+$\"");
        assertThat(firstError.getPropertyPath().toString()).isEqualTo("tags[].<iterable element>");
        assertThat(firstError.getInvalidValue()).isEqualTo("time");

        assertThat(secondError).isNotNull();
        assertThat(secondError.getMessage()).isEqualTo("must match \"^(?!^time$)[a-z]+[0-9a-z_]*[0-9a-z]+$\"");
        assertThat(secondError.getPropertyPath().toString()).isEqualTo("name");
        assertThat(secondError.getInvalidValue()).isEqualTo("time");
    }

    @Test
    public void testValidKeyName_givenEmptyName_expectValidationError() {
        SomeTestClass testObject = new SomeTestClass("");

        Set<ConstraintViolation<SomeTestClass>> violations = this.validator.validate(testObject);

        assertThat(violations).hasSize(2);

        Iterator<ConstraintViolation<SomeTestClass>> iterator = violations.iterator();

        ConstraintViolation<SomeTestClass> firstError = iterator.next();
        ConstraintViolation<SomeTestClass> secondError = iterator.next();

        assertThat(firstError).isNotNull();
        assertThat(firstError.getMessage()).isEqualTo("must not be blank");
        assertThat(firstError.getPropertyPath().toString()).isEqualTo("name");
        assertThat(firstError.getInvalidValue()).isEqualTo("");

        assertThat(secondError).isNotNull();
        assertThat(secondError.getMessage()).isEqualTo("must match \"^(?!^time$)[a-z]+[0-9a-z_]*[0-9a-z]+$\"");
        assertThat(secondError.getPropertyPath().toString()).isEqualTo("name");
        assertThat(secondError.getInvalidValue()).isEqualTo("");
    }

    private static class SomeTestClass {
        @ValidKeyName
        private final String name;

        private final Set<@ValidKeyName String> tags;

        SomeTestClass(String name) {
            this.name = name;
            this.tags = new HashSet<>();
        }

        SomeTestClass(String name, String... tagNames) {
            this.name = name;
            this.tags = new HashSet<>(Arrays.asList(tagNames));
        }

        public String getName()
        {
            return this.name;
        }

        public Set<String> getTags() {
            return this.tags;
        }
    }
}
