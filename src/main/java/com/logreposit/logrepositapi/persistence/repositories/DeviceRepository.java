package com.logreposit.logrepositapi.persistence.repositories;

import com.logreposit.logrepositapi.persistence.documents.Device;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeviceRepository extends MongoRepository<Device, String> {
  Page<Device> findByUserId(String userId, Pageable pageable);

  Optional<Device> findByIdAndUserId(String id, String userId);

  long countById(String id);

  long countByIdAndUserId(String id, String userId);
}
