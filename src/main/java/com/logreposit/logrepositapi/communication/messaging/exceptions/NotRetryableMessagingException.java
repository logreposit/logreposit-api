package com.logreposit.logrepositapi.communication.messaging.exceptions;

public class NotRetryableMessagingException extends MessagingException
{
    public NotRetryableMessagingException(String message)
    {
        super(message);
    }

    public NotRetryableMessagingException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
