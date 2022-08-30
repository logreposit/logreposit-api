package com.logreposit.logrepositapi.persistence.repositories;

import com.logreposit.logrepositapi.persistence.documents.DeviceToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeviceTokenRepository extends MongoRepository<DeviceToken, String> {
  Optional<DeviceToken> findByToken(String key);

  Optional<DeviceToken> findByIdAndDeviceId(String id, String deviceId);

  List<DeviceToken> findByDeviceId(String deviceId);

  Page<DeviceToken> findByDeviceId(String deviceId, Pageable pageable);

  void deleteByDeviceId(String deviceId);
}
