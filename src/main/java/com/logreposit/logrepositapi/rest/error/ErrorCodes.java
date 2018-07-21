package com.logreposit.logrepositapi.rest.error;

class ErrorCodes
{
    static final int USER_NOT_FOUND                          = 10001;
    static final int USER_ALREADY_EXISTENT                   = 10002;
    static final int API_KEY_NOT_FOUND                       = 20001;
    static final int DEVICE_NOT_FOUND                        = 30001;
    static final int DEVICE_TOKEN_NOT_FOUND                  = 40001;
    static final int INGRESS_ERROR                           = 50001;
    static final int LOGREPOSIT_ERROR                        = 60001;
    static final int ROUTE_NOT_FOUND                         = 80001;
    static final int HTTP_MEDIA_TYPE_NOT_SUPPORTED_ERROR     = 80002;
    static final int HTTP_REQUEST_METHOD_NOT_SUPPORTED_ERROR = 80003;
    static final int HTTP_MESSAGE_NOT_READABLE_ERROR         = 80004;
    static final int METHOD_ARGUMENT_NOT_VALID_ERROR         = 80005;
    static final int SERVLET_REQUEST_BINDING_ERROR           = 80006;
    static final int HTTP_MEDIA_TYPE_NOT_ACCEPTABLE_ERROR    = 80007;
    static final int MISSING_PATH_VARIABLE_ERROR             = 80008;
    static final int MISSING_SERVLET_REQUEST_PARAMETER_ERROR = 80009;
    static final int CONVERSION_NOT_SUPPORTED_ERROR          = 80010;
    static final int TYPE_MISMATCH_ERROR                     = 80011;
    static final int HTTP_MESSAGE_NOT_WRITABLE_ERROR         = 80012;
    static final int MISSING_SERVLET_REQUEST_PART_ERROR      = 80013;
    static final int BIND_ERROR                              = 80014;
    static final int ASYNC_REQUEST_TIMEOUT_ERROR             = 80015;
    static final int OTHER_ERROR                             = 99999;

    private ErrorCodes()
    {
    }
}
