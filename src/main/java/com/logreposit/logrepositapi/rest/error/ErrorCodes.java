package com.logreposit.logrepositapi.rest.error;

public class ErrorCodes
{
    public static final int USER_NOT_FOUND                          = 10001;
    public static final int USER_ALREADY_EXISTENT                   = 10002;
    public static final int API_KEY_NOT_FOUND                       = 20001;
    public static final int DEVICE_NOT_FOUND                        = 30001;
    public static final int DEVICE_TOKEN_NOT_FOUND                  = 40001;
    public static final int INGRESS_ERROR                           = 50001;
    public static final int LOGREPOSIT_ERROR                        = 60001;
    public static final int UNAUTHENTICATED_API_REQUEST             = 70001;
    public static final int UNAUTHORIZED_API_REQUEST                = 70002;
    public static final int UNAUTHORIZED_INGRESS_REQUEST            = 70003;
    public static final int ROUTE_NOT_FOUND                         = 80001;
    public static final int HTTP_MEDIA_TYPE_NOT_SUPPORTED_ERROR     = 80002;
    public static final int HTTP_REQUEST_METHOD_NOT_SUPPORTED_ERROR = 80003;
    public static final int HTTP_MESSAGE_NOT_READABLE_ERROR         = 80004;
    public static final int METHOD_ARGUMENT_NOT_VALID_ERROR         = 80005;
    public static final int SERVLET_REQUEST_BINDING_ERROR           = 80006;
    public static final int HTTP_MEDIA_TYPE_NOT_ACCEPTABLE_ERROR    = 80007;
    public static final int MISSING_PATH_VARIABLE_ERROR             = 80008;
    public static final int MISSING_SERVLET_REQUEST_PARAMETER_ERROR = 80009;
    public static final int CONVERSION_NOT_SUPPORTED_ERROR          = 80010;
    public static final int TYPE_MISMATCH_ERROR                     = 80011;
    public static final int HTTP_MESSAGE_NOT_WRITABLE_ERROR         = 80012;
    public static final int MISSING_SERVLET_REQUEST_PART_ERROR      = 80013;
    public static final int BIND_ERROR                              = 80014;
    public static final int ASYNC_REQUEST_TIMEOUT_ERROR             = 80015;
    public static final int OTHER_ERROR                             = 99999;

    private ErrorCodes()
    {
    }
}
