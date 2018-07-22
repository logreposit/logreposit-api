package com.logreposit.logrepositapi.rest.error;

import com.logreposit.logrepositapi.exceptions.LogrepositException;
import com.logreposit.logrepositapi.rest.dtos.common.ErrorResponse;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import com.logreposit.logrepositapi.services.device.DeviceNotFoundException;
import com.logreposit.logrepositapi.services.common.DeviceTokenNotFoundException;
import com.logreposit.logrepositapi.services.ingress.IngressServiceException;
import com.logreposit.logrepositapi.services.user.UserAlreadyExistentException;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;
import com.logreposit.logrepositapi.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalControllerExceptionHandler
{
    private static final Logger logger = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(HttpServletRequest request, UserNotFoundException exception)
    {
        LoggingUtils.getLogForException(exception);

        ErrorResponse errorResponse = ErrorResponseFactory.createUserNotFoundErrorResponse();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserAlreadyExistentException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistentException(HttpServletRequest request, UserAlreadyExistentException exception)
    {
        LoggingUtils.getLogForException(exception);

        ErrorResponse errorResponse = ErrorResponseFactory.createUserAlreadyExistentErrorResponse();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ApiKeyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleApiKeyNotFoundException(HttpServletRequest request, ApiKeyNotFoundException exception)
    {
        LoggingUtils.getLogForException(exception);

        ErrorResponse errorResponse = ErrorResponseFactory.createApiKeyNotFoundErrorResponse();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDeviceNotFoundException(HttpServletRequest request, DeviceNotFoundException exception)
    {
        LoggingUtils.getLogForException(exception);

        ErrorResponse errorResponse = ErrorResponseFactory.createDeviceNotFoundErrorResponse();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DeviceTokenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDeviceTokenNotFoundException(HttpServletRequest request, DeviceTokenNotFoundException exception)
    {
        LoggingUtils.getLogForException(exception);

        ErrorResponse errorResponse = ErrorResponseFactory.createDeviceTokenNotFoundErrorResponse();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IngressServiceException.class)
    public ResponseEntity<ErrorResponse> handleIngressServiceException(HttpServletRequest request, IngressServiceException exception)
    {
        LoggingUtils.getLogForException(exception);

        ErrorResponse errorResponse = ErrorResponseFactory.createIngressErrorResponse();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(LogrepositException.class)
    public ResponseEntity<ErrorResponse> handleLogrepositException(HttpServletRequest request, LogrepositException exception)
    {
        LoggingUtils.getLogForException(exception);

        ErrorResponse errorResponse = ErrorResponseFactory.createGlobalLogrepositErrorResponse();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
