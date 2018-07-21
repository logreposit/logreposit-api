package com.logreposit.logrepositapi.services.apikey;

import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.persistence.repositories.ApiKeyRepository;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;
import com.logreposit.logrepositapi.services.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
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
    public Page<ApiKey> list(String userId, int page, int size) throws UserNotFoundException
    {
        this.userService.get(userId);

        PageRequest  pageRequest = PageRequest.of(page, size);
        Page<ApiKey> apiKeys     = this.apiKeyRepository.findByUserId(userId, pageRequest);

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

    @Override
    public ApiKey get(String apiKeyId, String userId) throws UserNotFoundException, ApiKeyNotFoundException
    {
        this.userService.get(userId);

        Optional<ApiKey> apiKey = this.apiKeyRepository.findByIdAndUserId(apiKeyId, userId);

        if (!apiKey.isPresent())
        {
            logger.error("could not find api-key with id {}.", apiKeyId);
            throw new ApiKeyNotFoundException("could not find api-key with id");
        }

        return apiKey.get();
    }

    @Override
    public ApiKey delete(String apiKeyId, String userId) throws UserNotFoundException, ApiKeyNotFoundException
    {
        ApiKey apiKey = this.get(apiKeyId, userId);

        this.apiKeyRepository.delete(apiKey);

        return apiKey;
    }
}
