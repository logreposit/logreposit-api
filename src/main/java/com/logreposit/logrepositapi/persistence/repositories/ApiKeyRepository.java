package com.logreposit.logrepositapi.persistence.repositories;

import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ApiKeyRepository extends MongoRepository<ApiKey, String> {
  Optional<ApiKey> findByKey(String key);

  Optional<ApiKey> findByIdAndUserId(String id, String userId);

  Page<ApiKey> findByUserId(String userId, Pageable pageable);
}
