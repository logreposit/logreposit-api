package com.logreposit.logrepositapi.services.user;

import com.logreposit.logrepositapi.exceptions.LogrepositException;

public class UserServiceException extends LogrepositException
{
    public UserServiceException(String message)
    {
        super(message);
    }

    public UserServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
