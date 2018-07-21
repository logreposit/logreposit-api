package com.logreposit.logrepositapi.persistence.repositories;

import com.logreposit.logrepositapi.persistence.documents.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface DeviceRepository extends MongoRepository<Device, String>
{
    Page<Device>     findByUserId      (String userId, Pageable pageable);
    Optional<Device> findByIdAndUserId (String id, String userId);
}
