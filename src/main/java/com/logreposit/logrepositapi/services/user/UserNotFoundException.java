package com.logreposit.logrepositapi.services.user;

public class UserNotFoundException extends UserServiceException
{
    public UserNotFoundException(String message)
    {
        super(message);
    }
}
