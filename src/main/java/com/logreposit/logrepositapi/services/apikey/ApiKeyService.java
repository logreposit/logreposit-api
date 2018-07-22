package com.logreposit.logrepositapi.services.apikey;

import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;
import org.springframework.data.domain.Page;

public interface ApiKeyService
{
    ApiKey       create (String userId)                     throws UserNotFoundException;
    ApiKey       get    (String apiKeyId, String userId)    throws UserNotFoundException, ApiKeyNotFoundException;
    Page<ApiKey> list   (String userId, Integer page, Integer size) throws UserNotFoundException;
    ApiKey       delete (String apiKeyId, String userId)    throws UserNotFoundException, ApiKeyNotFoundException;
}
