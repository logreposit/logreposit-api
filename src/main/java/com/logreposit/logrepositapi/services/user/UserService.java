package com.logreposit.logrepositapi.services.user;

import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import org.springframework.data.domain.Page;

public interface UserService {
  CreatedUser create(User user) throws UserServiceException;

  User get(String userId) throws UserNotFoundException;

  User getByApiKey(String apiKey) throws ApiKeyNotFoundException, UserNotFoundException;

  User getFirstAdmin() throws UserNotFoundException;

  Page<User> list(Integer page, Integer size);
}
