package com.logreposit.logrepositapi.persistence.repositories;

import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ApiKeyRepository extends MongoRepository<ApiKey, String>
{
    Optional<ApiKey> findByKey         (String key);
    Optional<ApiKey> findByIdAndUserId (String id, String userId);
    Page<ApiKey>     findByUserId      (String userId, Pageable pageable);
}
