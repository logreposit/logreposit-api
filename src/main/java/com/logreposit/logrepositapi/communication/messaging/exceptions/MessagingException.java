package com.logreposit.logrepositapi.communication.messaging.exceptions;

import com.logreposit.logrepositapi.exceptions.LogrepositException;

public class MessagingException extends LogrepositException
{
    public MessagingException(String message)
    {
        super(message);
    }

    public MessagingException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
