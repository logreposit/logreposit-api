package com.logreposit.logrepositapi.communication.messaging.exceptions;

public class RetryableMessagingException extends MessagingException
{
    public RetryableMessagingException(String message)
    {
        super(message);
    }

    public RetryableMessagingException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
