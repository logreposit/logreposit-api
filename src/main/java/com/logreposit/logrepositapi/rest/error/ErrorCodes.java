package com.logreposit.logrepositapi.rest.error;

public class ErrorCodes
{
    public static final int USER_NOT_FOUND         = 10001;
    public static final int USER_ALREADY_EXISTENT  = 10002;
    public static final int API_KEY_NOT_FOUND      = 20001;
    public static final int DEVICE_NOT_FOUND       = 30001;
    public static final int DEVICE_TOKEN_NOT_FOUND = 40001;
    public static final int INGRESS_ERROR          = 50001;
    public static final int LOGREPOSIT_ERROR       = 60001;
    public static final int ROUTE_NOT_FOUND        = 80001;
    public static final int OTHER_ERROR            = 99999;

    private ErrorCodes()
    {
    }
}
