package com.logreposit.logrepositapi.services.user;

public class ApiKeyNotFoundException extends UserServiceException
{
    private final String apiKey;

    public ApiKeyNotFoundException(String message, String apiKey)
    {
        super(message);

        this.apiKey = apiKey;
    }

    public String getApiKey()
    {
        return this.apiKey;
    }
}
