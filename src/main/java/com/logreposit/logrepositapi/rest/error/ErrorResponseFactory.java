package com.logreposit.logrepositapi.rest.error;

import com.logreposit.logrepositapi.rest.dtos.common.ErrorResponse;

public class ErrorResponseFactory
{
    private ErrorResponseFactory()
    {
    }

    public static ErrorResponse createUserNotFoundErrorResponse()
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .code(ErrorCodes.USER_NOT_FOUND)
                                                   .message("Given user resource not found.")
                                                   .build();

        return errorResponse;
    }

    public static ErrorResponse createUserAlreadyExistentErrorResponse()
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .code(ErrorCodes.USER_ALREADY_EXISTENT)
                                                   .message("User resource with given email address is already existent. Please choose another email.")
                                                   .build();

        return errorResponse;
    }

    public static ErrorResponse createApiKeyNotFoundErrorResponse()
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .code(ErrorCodes.API_KEY_NOT_FOUND)
                                                   .message("Given api-key resource not found.")
                                                   .build();

        return errorResponse;
    }

    public static ErrorResponse createDeviceNotFoundErrorResponse()
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .code(ErrorCodes.DEVICE_NOT_FOUND)
                                                   .message("Given device resource not found.")
                                                   .build();

        return errorResponse;
    }

    public static ErrorResponse createDeviceTokenNotFoundErrorResponse()
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .code(ErrorCodes.DEVICE_TOKEN_NOT_FOUND)
                                                   .message("Given device-token resource not found.")
                                                   .build();

        return errorResponse;
    }

    public static ErrorResponse createIngressErrorResponse()
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .code(ErrorCodes.INGRESS_ERROR)
                                                   .message("Error processing data.")
                                                   .build();

        return errorResponse;
    }

    public static ErrorResponse createGlobalLogrepositErrorResponse()
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .code(ErrorCodes.LOGREPOSIT_ERROR)
                                                   .message("Some error occurred while processing your request. Please try again.")
                                                   .build();

        return errorResponse;
    }

    public static ErrorResponse createRouteNotFoundErrorResponse()
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .code(ErrorCodes.ROUTE_NOT_FOUND)
                                                   .message("Given route is not existent.")
                                                   .build();

        return errorResponse;
    }

    public static ErrorResponse createGlobalErrorResponse()
    {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                   .code(ErrorCodes.OTHER_ERROR)
                                                   .message("Some error occurred while processing your request. Please try again.")
                                                   .build();

        return errorResponse;
    }
}
