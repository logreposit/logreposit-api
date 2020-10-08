package com.logreposit.logrepositapi.services.user;

import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.persistence.documents.User;

public class CreatedUser
{
    private final User   user;
    private final ApiKey apiKey;

    public CreatedUser(User user, ApiKey apiKey)
    {
        this.user = user;
        this.apiKey = apiKey;
    }

    public User getUser()
    {
        return this.user;
    }

    public ApiKey getApiKey()
    {
        return this.apiKey;
    }
}
