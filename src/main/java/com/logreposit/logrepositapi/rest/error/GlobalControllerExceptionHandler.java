package com.logreposit.logrepositapi.rest.error;

import com.logreposit.logrepositapi.rest.dtos.common.ErrorResponse;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import com.logreposit.logrepositapi.services.common.DeviceTokenNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceNotFoundException;
import com.logreposit.logrepositapi.services.ingress.IngressServiceException;
import com.logreposit.logrepositapi.services.ingress.UnsupportedDeviceTypeException;
import com.logreposit.logrepositapi.services.user.UserAlreadyExistentException;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;
import com.logreposit.logrepositapi.utils.LoggingUtils;
import com.logreposit.logrepositapi.utils.definition.DefinitionUpdateValidationException;
import com.logreposit.logrepositapi.utils.definition.DefinitionValidationException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Order(value = Ordered.LOWEST_PRECEDENCE)
public class GlobalControllerExceptionHandler {
  private static final Logger logger =
      LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFoundException(
      HttpServletRequest request, UserNotFoundException exception) {
    logger.error(LoggingUtils.getLogForException(exception));

    ErrorResponse errorResponse = ErrorResponseFactory.createUserNotFoundErrorResponse();

    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UserAlreadyExistentException.class)
  public ResponseEntity<ErrorResponse> handleUserAlreadyExistentException(
      HttpServletRequest request, UserAlreadyExistentException exception) {
    logger.error(LoggingUtils.getLogForException(exception));

    ErrorResponse errorResponse = ErrorResponseFactory.createUserAlreadyExistentErrorResponse();

    return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(ApiKeyNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleApiKeyNotFoundException(
      HttpServletRequest request, ApiKeyNotFoundException exception) {
    logger.error(LoggingUtils.getLogForException(exception));

    ErrorResponse errorResponse = ErrorResponseFactory.createApiKeyNotFoundErrorResponse();

    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(DeviceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleDeviceNotFoundException(
      HttpServletRequest request, DeviceNotFoundException exception) {
    logger.error(LoggingUtils.getLogForException(exception));

    ErrorResponse errorResponse = ErrorResponseFactory.createDeviceNotFoundErrorResponse();

    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(DeviceTokenNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleDeviceTokenNotFoundException(
      HttpServletRequest request, DeviceTokenNotFoundException exception) {
    logger.error(LoggingUtils.getLogForException(exception));

    ErrorResponse errorResponse = ErrorResponseFactory.createDeviceTokenNotFoundErrorResponse();

    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(IngressServiceException.class)
  public ResponseEntity<ErrorResponse> handleIngressServiceException(
      HttpServletRequest request, IngressServiceException exception) {
    logger.error(LoggingUtils.getLogForException(exception));

    ErrorResponse errorResponse = ErrorResponseFactory.createIngressErrorResponse();

    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(UnsupportedDeviceTypeException.class)
  public ResponseEntity<ErrorResponse> handleUnsupportedDeviceTypeException(
      HttpServletRequest request, UnsupportedDeviceTypeException exception) {
    logger.error(LoggingUtils.getLogForException(exception));

    ErrorResponse errorResponse =
        ErrorResponseFactory.createIngressUnsupportedDeviceTypeErrorResponse(
            exception.getDeviceType());

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(DefinitionUpdateValidationException.class)
  public ResponseEntity<ErrorResponse> handleDefinitionUpdateValidationException(
      HttpServletRequest request, DefinitionUpdateValidationException exception) {
    logger.error(LoggingUtils.getLogForException(exception));

    ErrorResponse errorResponse =
        ErrorResponseFactory.createDeviceDefinitionUpdateErrorResponse(exception.getMessage());

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(DefinitionValidationException.class)
  public ResponseEntity<ErrorResponse> handleDefinitionValidationException(
      HttpServletRequest request, DefinitionValidationException exception) {
    logger.error(LoggingUtils.getLogForException(exception));

    ErrorResponse errorResponse =
        ErrorResponseFactory.createDeviceDefinitionValidationErrorResponse(exception.getMessage());

    return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolationException(
      HttpServletRequest request, ConstraintViolationException exception) {
    logger.error(LoggingUtils.getLogForException(exception));

    ErrorResponse errorResponse =
        ErrorResponseFactory.createConstraintViolationErrorResponse(exception);

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleOtherExceptions(
      HttpServletRequest request, Exception exception) {
    logger.error(LoggingUtils.getLogForException(exception));

    ErrorResponse errorResponse = ErrorResponseFactory.createGlobalErrorResponse();

    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
