package com.logreposit.logrepositapi.rest.error;

import com.logreposit.logrepositapi.rest.dtos.DeviceType;
import com.logreposit.logrepositapi.rest.dtos.common.ErrorResponse;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import jakarta.validation.ConstraintViolationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;

class ErrorResponseFactory {
  private ErrorResponseFactory() {}

  static ErrorResponse createUserNotFoundErrorResponse() {
    return ErrorResponse.builder()
        .code(ErrorCodes.USER_NOT_FOUND)
        .message("Given user resource not found.")
        .build();
  }

  static ErrorResponse createUserAlreadyExistentErrorResponse() {
    return ErrorResponse.builder()
        .code(ErrorCodes.USER_ALREADY_EXISTENT)
        .message(
            "User resource with given email address is already existent. Please choose another email.")
        .build();
  }

  static ErrorResponse createApiKeyNotFoundErrorResponse() {
    return ErrorResponse.builder()
        .code(ErrorCodes.API_KEY_NOT_FOUND)
        .message("Given api-key resource not found.")
        .build();
  }

  static ErrorResponse createDeviceNotFoundErrorResponse() {
    return ErrorResponse.builder()
        .code(ErrorCodes.DEVICE_NOT_FOUND)
        .message("Given device resource not found.")
        .build();
  }

  static ErrorResponse createDeviceTokenNotFoundErrorResponse() {
    return ErrorResponse.builder()
        .code(ErrorCodes.DEVICE_TOKEN_NOT_FOUND)
        .message("Given device-token resource not found.")
        .build();
  }

  static ErrorResponse createMqttCredentialNotFoundErrorResponse() {
    return ErrorResponse.builder()
        .code(ErrorCodes.MQTT_CREDENTIAL_NOT_FOUND)
        .message("Given mqtt-credential resource not found.")
        .build();
  }

  static ErrorResponse createIngressUnsupportedDeviceTypeErrorResponse(DeviceType deviceType) {
    return ErrorResponse.builder()
        .code(ErrorCodes.INGRESS_UNSUPPORTED_DEVICE_TYPE_ERROR)
        .message(String.format("Error processing data: Unsupported device type: '%s'", deviceType))
        .build();
  }

  static ErrorResponse createIngressErrorResponse() {
    return ErrorResponse.builder()
        .code(ErrorCodes.INGRESS_ERROR)
        .message("Error processing data.")
        .build();
  }

  static ErrorResponse createDeviceDefinitionUpdateErrorResponse(String message) {
    return ErrorResponse.builder()
        .code(ErrorCodes.INGRESS_DEVICE_DEFINITION_UPDATE_ERROR)
        .message(message)
        .build();
  }

  static ErrorResponse createDeviceDefinitionValidationErrorResponse(String message) {
    return ErrorResponse.builder()
        .code(ErrorCodes.INGRESS_DATA_VALIDATION_ERROR)
        .message(message)
        .build();
  }

  static ErrorResponse createRouteNotFoundErrorResponse() {
    return ErrorResponse.builder()
        .code(ErrorCodes.ROUTE_NOT_FOUND)
        .message("Given route is not existent.")
        .build();
  }

  static ErrorResponse createHttpRequestMethodNotSupportedErrorResponse(
      HttpRequestMethodNotSupportedException e) {
    final var supportedMethods =
        Arrays.stream(Objects.requireNonNull(e.getSupportedMethods()))
            .sorted()
            .collect(Collectors.joining(", "));

    final var errorMessage =
        String.format(
            "Given HTTP Method '%s' is not supported on this particular route. Supported HTTP Methods are: %s",
            e.getMethod(), supportedMethods);

    return ErrorResponse.builder()
        .code(ErrorCodes.HTTP_REQUEST_METHOD_NOT_SUPPORTED_ERROR)
        .message(errorMessage)
        .build();
  }

  static ErrorResponse createHttpMessageNotReadableErrorResponse(
      HttpMessageNotReadableException e) {
    final var errorMessage =
        "Request could not be processed. Please check if the JSON syntax is valid.";

    return ErrorResponse.builder()
        .code(ErrorCodes.HTTP_MESSAGE_NOT_READABLE_ERROR)
        .message(errorMessage)
        .build();
  }

  static ErrorResponse createHttpMediaTypeNotSupportedErrorResponse(
      HttpMediaTypeNotSupportedException e) {
    StringBuilder stringBuilder = new StringBuilder();

    stringBuilder.append("Given MediaType ");

    if (e.getContentType() != null) {
      final var mediaType = e.getContentType().toString();

      stringBuilder.append("'");
      stringBuilder.append(mediaType);
      stringBuilder.append("'");
    }

    stringBuilder.append(" is not supported. Supported MediaTypes are: ");

    stringBuilder.append(
        e.getSupportedMediaTypes().stream()
            .filter(Objects::nonNull)
            .map(MediaType::toString)
            .sorted()
            .collect(Collectors.joining(", ")));

    final var errorMessage = stringBuilder.toString();

    return ErrorResponse.builder()
        .code(ErrorCodes.HTTP_MEDIA_TYPE_NOT_SUPPORTED_ERROR)
        .message(errorMessage)
        .build();
  }

  static ErrorResponse createMethodArgumentNotValidErrorResponse(
      MethodArgumentNotValidException e) {
    final var fieldErrors = e.getBindingResult().getFieldErrors();
    final var globalErrors = e.getBindingResult().getGlobalErrors();

    final var stringBuilder = new StringBuilder();

    stringBuilder.append("Invalid input data. ");

    final var globalErrorsMessage =
        globalErrors.stream()
            .map(
                error ->
                    String.format("%s -> %s", error.getObjectName(), error.getDefaultMessage()))
            .collect(Collectors.joining(", "));

    final var fieldErrorsMessage =
        fieldErrors.stream()
            .map(
                error ->
                    String.format(
                        "%s -> %s (actual value: %s)",
                        error.getField(), error.getDefaultMessage(), error.getRejectedValue()))
            .collect(Collectors.joining((", ")));

    if (StringUtils.isNotEmpty(globalErrorsMessage)) {
      stringBuilder.append("Global Errors: ");
      stringBuilder.append(globalErrorsMessage);
    }

    if (StringUtils.isNotEmpty(fieldErrorsMessage)) {
      if (StringUtils.isNotEmpty(globalErrorsMessage)) {
        stringBuilder.append("; ");
      }

      stringBuilder.append("Field Errors: ");
      stringBuilder.append(fieldErrorsMessage);
    }

    stringBuilder.append(" => Please check your input.");

    final var errorMessage = stringBuilder.toString();

    return ErrorResponse.builder()
        .code(ErrorCodes.METHOD_ARGUMENT_NOT_VALID_ERROR)
        .message(errorMessage)
        .build();
  }

  static ErrorResponse createServletRequestBindingErrorResponse(ServletRequestBindingException e) {
    return ErrorResponse.builder()
        .code(ErrorCodes.SERVLET_REQUEST_BINDING_ERROR)
        .message(e.getMessage())
        .build();
  }

  static ErrorResponse createHttpMediaTypeNotAcceptableErrorResponse(
      HttpMediaTypeNotAcceptableException e) {
    final var errorMessage =
        String.format(
            "Given HTTP MediaType is not acceptable. Supported MediaTypes are: %s",
            e.getSupportedMediaTypes().stream()
                .map(MediaType::toString)
                .collect(Collectors.joining(", ")));

    return ErrorResponse.builder()
        .code(ErrorCodes.HTTP_MEDIA_TYPE_NOT_ACCEPTABLE_ERROR)
        .message(errorMessage)
        .build();
  }

  static ErrorResponse createGlobalErrorResponse() {
    return ErrorResponse.builder()
        .code(ErrorCodes.OTHER_ERROR)
        .message("Some error occurred while processing your request. Please try again.")
        .build();
  }

  static ErrorResponse createErrorResponse(int code, String message) {
    return ErrorResponse.builder().code(code).message(message).build();
  }

  static ErrorResponse createConstraintViolationErrorResponse(ConstraintViolationException e) {
    return ErrorResponse.builder()
        .code(ErrorCodes.CONSTRAINT_VIOLATION_ERROR)
        .message(e.getMessage())
        .build();
  }
}
