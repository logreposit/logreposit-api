package com.logreposit.logrepositapi.persistence.repositories;

import com.logreposit.logrepositapi.persistence.documents.MqttCredential;
import com.logreposit.logrepositapi.persistence.documents.MqttRole;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MqttCredentialRepository extends MongoRepository<MqttCredential, String> {
  Optional<MqttCredential> findByIdAndUserId(String id, String userId);

  Optional<MqttCredential> findFirstByRolesContaining(MqttRole role);

  Page<MqttCredential> findByUserId(String userId, Pageable pageable);

  Stream<MqttCredential> findAllBy();
}
