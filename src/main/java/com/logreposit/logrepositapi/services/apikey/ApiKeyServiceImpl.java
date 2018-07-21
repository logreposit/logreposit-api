package com.logreposit.logrepositapi.services.apikey;

import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.persistence.repositories.ApiKeyRepository;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;
import com.logreposit.logrepositapi.services.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ApiKeyServiceImpl implements ApiKeyService
{
    private static final Logger logger = LoggerFactory.getLogger(ApiKeyServiceImpl.class);

    private final ApiKeyRepository apiKeyRepository;
    private final UserService      userService;

    public ApiKeyServiceImpl(ApiKeyRepository apiKeyRepository, UserService userService)
    {
        this.apiKeyRepository = apiKeyRepository;
        this.userService      = userService;
    }

    @Override
    public List<ApiKey> list(String userId) throws UserNotFoundException
    {
        this.userService.get(userId);

        List<ApiKey> apiKeys = this.apiKeyRepository.findByUserId(userId);

        return apiKeys;
    }

    @Override
    public ApiKey create(String userId) throws UserNotFoundException
    {
        this.userService.get(userId);

        ApiKey apiKey = new ApiKey();
        apiKey.setCreatedAt(new Date());
        apiKey.setUserId(userId);
        apiKey.setKey(UUID.randomUUID().toString());

        ApiKey createdApikey = this.apiKeyRepository.save(apiKey);

        logger.info("Successfully created new api key: {}", createdApikey);

        return createdApikey;
    }
}
