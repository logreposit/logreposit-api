package com.logreposit.logrepositapi.persistence.repositories;

import com.logreposit.logrepositapi.persistence.documents.User;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
  long countByEmail(String email);

  Optional<User> findFirstByRolesContaining(String role);
}
