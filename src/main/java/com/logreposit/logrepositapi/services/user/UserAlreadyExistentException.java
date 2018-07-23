package com.logreposit.logrepositapi.services.user;

public class UserAlreadyExistentException extends UserServiceException
{
    private final String email;

    public UserAlreadyExistentException(String message, String email)
    {
        super(message);

        this.email = email;
    }

    public String getEmail()
    {
        return this.email;
    }
}
