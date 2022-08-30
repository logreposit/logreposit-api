package com.logreposit.logrepositapi.rest.error;

import com.logreposit.logrepositapi.rest.dtos.common.ErrorResponse;
import com.logreposit.logrepositapi.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Order(value = Ordered.LOWEST_PRECEDENCE - 1)
public class LogrepositResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
  private static final Logger logger =
      LoggerFactory.getLogger(LogrepositResponseEntityExceptionHandler.class);

  @Override
  protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
      HttpRequestMethodNotSupportedException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    logger.error(LoggingUtils.getLogForException(ex));

    ErrorResponse errorResponse =
        ErrorResponseFactory.createHttpRequestMethodNotSupportedErrorResponse(ex);

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    logger.error(LoggingUtils.getLogForException(ex));

    ErrorResponse errorResponse =
        ErrorResponseFactory.createHttpMediaTypeNotSupportedErrorResponse(ex);

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
      HttpMediaTypeNotAcceptableException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    logger.error(LoggingUtils.getLogForException(ex));

    ErrorResponse errorResponse =
        ErrorResponseFactory.createHttpMediaTypeNotAcceptableErrorResponse(ex);

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @Override
  protected ResponseEntity<Object> handleMissingPathVariable(
      MissingPathVariableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
    logger.error(LoggingUtils.getLogForException(ex));

    ErrorResponse errorResponse =
        ErrorResponseFactory.createErrorResponse(
            ErrorCodes.MISSING_PATH_VARIABLE_ERROR, ex.getMessage());

    return new ResponseEntity<>(errorResponse, status);
  }

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestParameter(
      MissingServletRequestParameterException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    logger.error(LoggingUtils.getLogForException(ex));

    ErrorResponse errorResponse =
        ErrorResponseFactory.createErrorResponse(
            ErrorCodes.MISSING_SERVLET_REQUEST_PARAMETER_ERROR, ex.getMessage());

    return new ResponseEntity<>(errorResponse, status);
  }

  @Override
  protected ResponseEntity<Object> handleServletRequestBindingException(
      ServletRequestBindingException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    logger.error(LoggingUtils.getLogForException(ex));

    ErrorResponse errorResponse = ErrorResponseFactory.createServletRequestBindingErrorResponse(ex);

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @Override
  protected ResponseEntity<Object> handleConversionNotSupported(
      ConversionNotSupportedException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    logger.error(LoggingUtils.getLogForException(ex));

    ErrorResponse errorResponse =
        ErrorResponseFactory.createErrorResponse(
            ErrorCodes.CONVERSION_NOT_SUPPORTED_ERROR, ex.getMessage());

    return new ResponseEntity<>(errorResponse, status);
  }

  @Override
  protected ResponseEntity<Object> handleTypeMismatch(
      TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
    logger.error(LoggingUtils.getLogForException(ex));

    ErrorResponse errorResponse =
        ErrorResponseFactory.createErrorResponse(ErrorCodes.TYPE_MISMATCH_ERROR, ex.getMessage());

    return new ResponseEntity<>(errorResponse, status);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    logger.error(LoggingUtils.getLogForException(ex));

    ErrorResponse errorResponse =
        ErrorResponseFactory.createHttpMessageNotReadableErrorResponse(ex);

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotWritable(
      HttpMessageNotWritableException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    logger.error(LoggingUtils.getLogForException(ex));

    ErrorResponse errorResponse =
        ErrorResponseFactory.createErrorResponse(
            ErrorCodes.HTTP_MESSAGE_NOT_WRITABLE_ERROR, ex.getMessage());

    return new ResponseEntity<>(errorResponse, status);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    logger.error(LoggingUtils.getLogForException(ex));

    ErrorResponse errorResponse =
        ErrorResponseFactory.createMethodArgumentNotValidErrorResponse(ex);

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestPart(
      MissingServletRequestPartException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    logger.error(LoggingUtils.getLogForException(ex));

    ErrorResponse errorResponse =
        ErrorResponseFactory.createErrorResponse(
            ErrorCodes.MISSING_SERVLET_REQUEST_PART_ERROR, ex.getMessage());

    return new ResponseEntity<>(errorResponse, status);
  }

  @Override
  protected ResponseEntity<Object> handleBindException(
      BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
    logger.error(LoggingUtils.getLogForException(ex));

    ErrorResponse errorResponse =
        ErrorResponseFactory.createErrorResponse(ErrorCodes.BIND_ERROR, ex.getMessage());

    return new ResponseEntity<>(errorResponse, status);
  }

  @Override
  protected ResponseEntity<Object> handleNoHandlerFoundException(
      NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
    logger.error(LoggingUtils.getLogForException(ex));

    ErrorResponse errorResponse = ErrorResponseFactory.createRouteNotFoundErrorResponse();

    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @Override
  protected ResponseEntity<Object> handleAsyncRequestTimeoutException(
      AsyncRequestTimeoutException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest webRequest) {
    logger.error(LoggingUtils.getLogForException(ex));

    ErrorResponse errorResponse =
        ErrorResponseFactory.createErrorResponse(
            ErrorCodes.ASYNC_REQUEST_TIMEOUT_ERROR, ex.getMessage());

    return new ResponseEntity<>(errorResponse, status);
  }

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
    logger.error(LoggingUtils.getLogForException(ex));

    ErrorResponse errorResponse = ErrorResponseFactory.createGlobalErrorResponse();

    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
