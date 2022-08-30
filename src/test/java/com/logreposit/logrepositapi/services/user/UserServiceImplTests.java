package com.logreposit.logrepositapi.services.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.dtos.UserCreatedMessageDto;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessageSenderException;
import com.logreposit.logrepositapi.communication.messaging.rabbitmq.RabbitMessageSender;
import com.logreposit.logrepositapi.communication.messaging.utils.MessageFactory;
import com.logreposit.logrepositapi.persistence.documents.ApiKey;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.persistence.repositories.ApiKeyRepository;
import com.logreposit.logrepositapi.persistence.repositories.UserRepository;
import com.logreposit.logrepositapi.rest.security.UserRoles;
import com.logreposit.logrepositapi.services.common.ApiKeyNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class UserServiceImplTests {
  @MockBean private UserRepository userRepository;

  @MockBean private ApiKeyRepository apiKeyRepository;

  @MockBean private MessageFactory messageFactory;

  @MockBean private RabbitMessageSender messageSender;

  @Captor private ArgumentCaptor<ApiKey> apiKeyArgumentCaptor;

  @Captor private ArgumentCaptor<PageRequest> pageRequestArgumentCaptor;

  private UserServiceImpl userService;

  @BeforeEach
  public void setUp() {
    this.userService =
        new UserServiceImpl(
            this.userRepository, this.apiKeyRepository, this.messageFactory, this.messageSender);
  }

  @Test
  public void testCreate()
      throws UserServiceException, JsonProcessingException, MessageSenderException {
    String email = UUID.randomUUID().toString() + "@local.local";
    List<String> roles = Arrays.asList("ROLE1", "ROLE2");
    String plainTextPassword = UUID.randomUUID().toString();

    User user = new User();
    user.setEmail(email);
    user.setRoles(roles);
    user.setPassword(plainTextPassword);

    User createdUser = new User();
    createdUser.setId(UUID.randomUUID().toString());
    createdUser.setEmail(email);
    createdUser.setRoles(roles);

    Message userCreatedMessage = new Message();

    Mockito.when(this.userRepository.save(Mockito.eq(user))).thenReturn(createdUser);
    Mockito.when(
            this.messageFactory.buildEventUserCreatedMessage(
                Mockito.any(UserCreatedMessageDto.class)))
        .thenReturn(userCreatedMessage);

    Mockito.when(this.apiKeyRepository.save(Mockito.any()))
        .thenAnswer(
            i -> {
              ApiKey firstArgument = (ApiKey) i.getArguments()[0];

              firstArgument.setId(UUID.randomUUID().toString());

              return firstArgument;
            });

    CreatedUser result = this.userService.create(user);

    Mockito.verify(this.userRepository, Mockito.times(1)).countByEmail(Mockito.eq(email));
    Mockito.verify(this.userRepository, Mockito.times(1)).save(Mockito.eq(user));
    Mockito.verify(this.apiKeyRepository, Mockito.times(1))
        .save(this.apiKeyArgumentCaptor.capture());

    ApiKey capturedApiKey = this.apiKeyArgumentCaptor.getValue();

    assertThat(capturedApiKey).isNotNull();
    assertThat(capturedApiKey.getId()).isNotNull();
    assertThat(capturedApiKey.getUserId()).isEqualTo(createdUser.getId());
    assertThat(result.getUser()).isSameAs(createdUser);
    assertThat(result.getApiKey()).isNotNull();

    ArgumentCaptor<UserCreatedMessageDto> userCreatedMessageDtoArgumentCaptor =
        ArgumentCaptor.forClass(UserCreatedMessageDto.class);

    Mockito.verify(this.messageFactory, Mockito.times(1))
        .buildEventUserCreatedMessage(userCreatedMessageDtoArgumentCaptor.capture());
    Mockito.verify(this.messageSender, Mockito.times(1)).send(Mockito.same(userCreatedMessage));

    UserCreatedMessageDto capturedUserCreatedMessageDto =
        userCreatedMessageDtoArgumentCaptor.getValue();

    assertThat(capturedUserCreatedMessageDto).isNotNull();

    assertThat(capturedUserCreatedMessageDto.getPassword()).isEqualTo(plainTextPassword);
    assertThat(capturedUserCreatedMessageDto.getId()).isEqualTo(createdUser.getId());
    assertThat(capturedUserCreatedMessageDto.getEmail()).isEqualTo(createdUser.getEmail());
    assertThat(capturedUserCreatedMessageDto.getRoles()).isEqualTo(createdUser.getRoles());
  }

  @Test
  public void testCreate_emailAlreadyExistent() {
    String email = UUID.randomUUID().toString() + "@local.local";
    List<String> roles = Arrays.asList("ROLE1", "ROLE2");

    User user = new User();
    user.setEmail(email);
    user.setRoles(roles);

    Mockito.when(this.userRepository.countByEmail(Mockito.eq(email))).thenReturn(1L);

    assertThrows(UserAlreadyExistentException.class, () -> this.userService.create(user));
  }

  @Test
  public void testList() {
    User user1 = new User();
    user1.setId(UUID.randomUUID().toString());
    user1.setEmail("email1@local");
    user1.setRoles(Arrays.asList("USER", "ADMIN"));

    User user2 = new User();
    user2.setId(UUID.randomUUID().toString());
    user2.setEmail("email2@local");
    user2.setRoles(Arrays.asList("USER", "SOMEOTHERROLE"));

    List<User> users = Arrays.asList(user1, user2);
    Page<User> userPage = new PageImpl<>(users);

    int page = 2;
    int size = 15;

    Mockito.when(this.userRepository.findAll(Mockito.any(PageRequest.class))).thenReturn(userPage);

    Page<User> result = this.userService.list(page, size);

    assertThat(result).isNotNull();

    Mockito.verify(this.userRepository, Mockito.times(1))
        .findAll(this.pageRequestArgumentCaptor.capture());

    PageRequest capturedPageRequest = this.pageRequestArgumentCaptor.getValue();

    assertThat(capturedPageRequest).isNotNull();
    assertThat(capturedPageRequest.getPageNumber()).isEqualTo(page);
    assertThat(capturedPageRequest.getPageSize()).isEqualTo(size);
    assertThat(result).isSameAs(userPage);
  }

  @Test
  public void testGetByApiKey() throws UserNotFoundException, ApiKeyNotFoundException {
    String apiKey = UUID.randomUUID().toString();

    User existentUser = new User();
    existentUser.setId(UUID.randomUUID().toString());
    existentUser.setRoles(Arrays.asList("ROLE0", "ROLE1"));
    existentUser.setEmail("existent@local");

    ApiKey existentApiKey = new ApiKey();
    existentApiKey.setId(UUID.randomUUID().toString());
    existentApiKey.setKey(apiKey);
    existentApiKey.setUserId(existentUser.getId());
    existentApiKey.setCreatedAt(new Date());

    Mockito.when(this.apiKeyRepository.findByKey(Mockito.eq(apiKey)))
        .thenReturn(Optional.of(existentApiKey));
    Mockito.when(this.userRepository.findById(Mockito.eq(existentApiKey.getUserId())))
        .thenReturn(Optional.of(existentUser));

    User user = this.userService.getByApiKey(apiKey);

    assertThat(user).isNotNull();
    assertThat(user).isSameAs(existentUser);

    Mockito.verify(this.apiKeyRepository, Mockito.times(1)).findByKey(Mockito.eq(apiKey));
    Mockito.verify(this.userRepository, Mockito.times(1))
        .findById(Mockito.eq(existentApiKey.getUserId()));
  }

  @Test
  public void testGetByApiKey_noSuchKey() {
    String apiKey = UUID.randomUUID().toString();

    Mockito.when(this.apiKeyRepository.findByKey(Mockito.eq(apiKey))).thenReturn(Optional.empty());

    assertThrows(ApiKeyNotFoundException.class, () -> this.userService.getByApiKey(apiKey));
  }

  @Test
  public void testGetByApiKey_noSuchUser() {
    String apiKey = UUID.randomUUID().toString();

    ApiKey existentApiKey = new ApiKey();
    existentApiKey.setId(UUID.randomUUID().toString());
    existentApiKey.setKey(apiKey);
    existentApiKey.setUserId(UUID.randomUUID().toString());
    existentApiKey.setCreatedAt(new Date());

    Mockito.when(this.apiKeyRepository.findByKey(Mockito.eq(apiKey)))
        .thenReturn(Optional.of(existentApiKey));
    Mockito.when(this.userRepository.findById(Mockito.eq(existentApiKey.getUserId())))
        .thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> this.userService.getByApiKey(apiKey));
  }

  @Test
  public void testGetFirstAdmin() throws UserNotFoundException {
    User user = new User();
    user.setEmail("admin@localhost");
    user.setRoles(Collections.singletonList(UserRoles.ADMIN));
    user.setId(UUID.randomUUID().toString());

    Mockito.when(this.userRepository.findFirstByRolesContaining(Mockito.eq(UserRoles.ADMIN)))
        .thenReturn(Optional.of(user));

    User result = this.userService.getFirstAdmin();

    assertThat(result).isNotNull();
    assertThat(result).isSameAs(user);

    Mockito.verify(this.userRepository, Mockito.times(1))
        .findFirstByRolesContaining(Mockito.eq(UserRoles.ADMIN));
  }

  @Test
  public void testGetFirstAdmin_noSuchUser() {
    Mockito.when(this.userRepository.findFirstByRolesContaining(Mockito.eq(UserRoles.ADMIN)))
        .thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> this.userService.getFirstAdmin());
  }
}
