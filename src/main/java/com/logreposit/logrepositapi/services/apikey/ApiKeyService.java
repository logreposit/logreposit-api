package com.logreposit.logrepositapi.services.apikey;

import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;

import java.util.List;

public interface ApiKeyService
{
    ApiKey       create (String userId) throws UserNotFoundException;
    List<ApiKey> list   (String userId) throws UserNotFoundException;
}
