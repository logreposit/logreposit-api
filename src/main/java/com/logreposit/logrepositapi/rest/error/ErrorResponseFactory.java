package com.logreposit.logrepositapi.rest.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.rest.dtos.common.ErrorResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.MimeType;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class ErrorResponseFactory
{
    private ErrorResponseFactory()
    {
    }

    static ErrorResponse createUserNotFoundErrorResponse()
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .code(ErrorCodes.USER_NOT_FOUND)
                                                   .message("Given user resource not found.")
                                                   .build();

        return errorResponse;
    }

    static ErrorResponse createUserAlreadyExistentErrorResponse()
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .code(ErrorCodes.USER_ALREADY_EXISTENT)
                                                   .message("User resource with given email address is already existent. Please choose another email.")
                                                   .build();

        return errorResponse;
    }

    static ErrorResponse createApiKeyNotFoundErrorResponse()
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .code(ErrorCodes.API_KEY_NOT_FOUND)
                                                   .message("Given api-key resource not found.")
                                                   .build();

        return errorResponse;
    }

    static ErrorResponse createDeviceNotFoundErrorResponse()
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .code(ErrorCodes.DEVICE_NOT_FOUND)
                                                   .message("Given device resource not found.")
                                                   .build();

        return errorResponse;
    }

    static ErrorResponse createDeviceTokenNotFoundErrorResponse()
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .code(ErrorCodes.DEVICE_TOKEN_NOT_FOUND)
                                                   .message("Given device-token resource not found.")
                                                   .build();

        return errorResponse;
    }

    static ErrorResponse createIngressErrorResponse()
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .code(ErrorCodes.INGRESS_ERROR)
                                                   .message("Error processing data.")
                                                   .build();

        return errorResponse;
    }

    static ErrorResponse createGlobalLogrepositErrorResponse()
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .code(ErrorCodes.LOGREPOSIT_ERROR)
                                                   .message("Some error occurred while processing your request. Please try again.")
                                                   .build();

        return errorResponse;
    }

    static ErrorResponse createRouteNotFoundErrorResponse()
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .code(ErrorCodes.ROUTE_NOT_FOUND)
                                                   .message("Given route is not existent.")
                                                   .build();

        return errorResponse;
    }

    static ErrorResponse createHttpRequestMethodNotSupportedErrorResponse(HttpRequestMethodNotSupportedException e)
    {
        String supportedMethods = Arrays.stream(Objects.requireNonNull(e.getSupportedMethods())).collect(Collectors.joining(", "));

        String errorMessage = String.format("Given HTTP Method '%s' is not supported on this particular route. Supported HTTP Methods are: %s",
                                            e.getMethod(), supportedMethods);

        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .code(ErrorCodes.HTTP_REQUEST_METHOD_NOT_SUPPORTED_ERROR)
                                                   .message(errorMessage)
                                                   .build();

        return errorResponse;
    }

    static ErrorResponse createHttpMessageNotReadableErrorResponse(HttpMessageNotReadableException e)
    {
        Throwable mostSpecificCause = e.getMostSpecificCause();
        String    exceptionMessage  = mostSpecificCause.getMessage();
        String    errorMessage      = String.format("HTTP Message is not readable: %s", exceptionMessage);

        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .code(ErrorCodes.HTTP_MESSAGE_NOT_READABLE_ERROR)
                                                   .message(errorMessage)
                                                   .build();

        return errorResponse;
    }

    static ErrorResponse createHttpMediaTypeNotSupportedErrorResponse(HttpMediaTypeNotSupportedException e)
    {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Given MediaType ");

        if (e.getContentType() != null)
        {
            String mediaType = e.getContentType().toString();

            stringBuilder.append("'");
            stringBuilder.append(mediaType);
            stringBuilder.append("'");
        }

        stringBuilder.append(" is not supported. Supported MediaTypes are: ");
        stringBuilder.append(e.getSupportedMediaTypes()
                              .stream()
                              .filter(Objects::nonNull)
                              .map(MediaType::toString)
                              .collect(Collectors.joining(", ")));

        String errorMessage = stringBuilder.toString();

        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .code(ErrorCodes.HTTP_MEDIA_TYPE_NOT_SUPPORTED_ERROR)
                                                   .message(errorMessage)
                                                   .build();

        return errorResponse;
    }

    static ErrorResponse createMethodArgumentNotValidErrorResponse(MethodArgumentNotValidException e)
    {
        List<FieldError>  fieldErrors  = e.getBindingResult().getFieldErrors();
        List<ObjectError> globalErrors = e.getBindingResult().getGlobalErrors();

        // TODO make more beautiful. !!11elf

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Invalid input data. ");
        stringBuilder.append("Global Errors: ");

        String globalErrorsMessage = globalErrors.stream()
                                                 .map(error -> String.format("%s -> %s", error.getObjectName(), error.getDefaultMessage()))
                                                 .collect(Collectors.joining(", "));

        stringBuilder.append(globalErrorsMessage);
        stringBuilder.append("; Field Errors: ");

        String fieldErrorsMessage = fieldErrors.stream()
                                               .map(error -> String.format("%s -> %s (!= %s)", error.getField(), error.getDefaultMessage(), error.getRejectedValue()))
                                               .collect(Collectors.joining((", ")));

        stringBuilder.append(fieldErrorsMessage);

        stringBuilder.append("; Please check your input.");

        String errorMessage= stringBuilder.toString();

        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .code(ErrorCodes.METHOD_ARGUMENT_NOT_VALID_ERROR)
                                                   .message(errorMessage)
                                                   .build();

        return errorResponse;
    }

    static ErrorResponse createServletRequestBindingErrorResponse(ServletRequestBindingException e)
    {
        // TODO make more beautiful. !!11elf

        String errorMessage = e.getMessage();

        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .code(ErrorCodes.SERVLET_REQUEST_BINDING_ERROR)
                                                   .message(errorMessage)
                                                   .build();

        return errorResponse;
    }

    static ErrorResponse createHttpMediaTypeNotAcceptableErrorResponse(HttpMediaTypeNotAcceptableException e)
    {
        String errorMessage = String.format("Given HTTP MediaType is not acceptable. Supported MediaTypes are: %s",
                                             e.getSupportedMediaTypes().stream().map(MediaType::toString).collect(Collectors.joining(", ")));

        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .code(ErrorCodes.HTTP_MEDIA_TYPE_NOT_ACCEPTABLE_ERROR)
                                                   .message(errorMessage)
                                                   .build();

        return errorResponse;
    }

    static ErrorResponse createGlobalErrorResponse()
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .code(ErrorCodes.OTHER_ERROR)
                                                   .message("Some error occurred while processing your request. Please try again.")
                                                   .build();

        return errorResponse;
    }
}
