package com.logreposit.logrepositapi.persistence.repositories;

import com.logreposit.logrepositapi.persistence.documents.MqttCredential;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MqttCredentialRepository extends MongoRepository<MqttCredential, String> {
  Optional<MqttCredential> findByIdAndUserId(String id, String userId);

  Page<MqttCredential> findByUserId(String userId, Pageable pageable);
}
