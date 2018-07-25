package com.logreposit.logrepositapi.services.apikey;

import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import org.springframework.data.domain.Page;

public interface ApiKeyService
{
    ApiKey       create           (String userId);
    Page<ApiKey> list             (String userId, Integer page, Integer size);

    ApiKey       get              (String apiKeyId, String userId) throws ApiKeyNotFoundException;
    ApiKey       delete           (String apiKeyId, String userId) throws ApiKeyNotFoundException;
}
