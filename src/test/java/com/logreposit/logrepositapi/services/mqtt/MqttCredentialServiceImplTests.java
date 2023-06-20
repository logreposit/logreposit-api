package com.logreposit.logrepositapi.services.mqtt;

import static com.logreposit.logrepositapi.persistence.documents.MqttRole.ACCOUNT_DEVICE_DATA_READ;
import static com.logreposit.logrepositapi.persistence.documents.MqttRole.GLOBAL_DEVICE_DATA_WRITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logreposit.logrepositapi.persistence.documents.MqttCredential;
import com.logreposit.logrepositapi.persistence.documents.MqttRole;
import com.logreposit.logrepositapi.persistence.repositories.MqttCredentialRepository;
import com.logreposit.logrepositapi.services.mqtt.emqx.EmqxApiClient;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.AuthAction;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.AuthPermission;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.EmqxAuthRule;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.EmqxAuthUser;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
public class MqttCredentialServiceImplTests {
  private MqttCredentialService mqttCredentialService;

  @Mock private MqttCredentialRepository mqttCredentialRepository;
  @Mock private EmqxApiClient emqxApiClient;

  @Captor private ArgumentCaptor<List<EmqxAuthRule>> authRulesCaptor;

  @BeforeEach
  public void setUp() {
    this.mqttCredentialService =
        new MqttCredentialServiceImpl(mqttCredentialRepository, emqxApiClient);
  }

  @Test
  public void testCreate_givenNoExistingEmqxAuthUser_expectSucceeds() {
    final var userId = "myUserId1";
    final var randomId = UUID.randomUUID().toString();
    final var now = new Date();

    final var mqttUsernamePattern = mqttUsernamePattern(userId);

    when(emqxApiClient.retrieveEmqxAuthUser(matches(mqttUsernamePattern)))
        .thenReturn(Optional.empty());

    when(emqxApiClient.createEmqxAuthUser(matches(mqttUsernamePattern(userId)), anyString()))
        .thenAnswer(i -> EmqxAuthUser.builder().userId(i.getArgument(0)).superuser(false).build());

    when(emqxApiClient.listRulesOfAuthUser(matches(mqttUsernamePattern))).thenReturn(List.of());
    doNothing().when(emqxApiClient).deleteRulesOfAuthUser(matches(mqttUsernamePattern));
    doNothing().when(emqxApiClient).createRulesForAuthUser(matches(mqttUsernamePattern), anyList());

    when(mqttCredentialRepository.save(any(MqttCredential.class)))
        .thenAnswer(
            i -> {
              final var input = i.getArgument(0, MqttCredential.class);

              return MqttCredential.builder()
                  .id(randomId)
                  .createdAt(now)
                  .userId(input.getUserId())
                  .description(input.getDescription())
                  .username(input.getUsername())
                  .password(input.getPassword())
                  .roles(input.getRoles())
                  .build();
            });

    final var createdCredential =
        mqttCredentialService.create(
            userId,
            "some descriptional text",
            List.of(GLOBAL_DEVICE_DATA_WRITE, ACCOUNT_DEVICE_DATA_READ));

    final var passwordCaptor = ArgumentCaptor.forClass(String.class);

    verify(emqxApiClient).retrieveEmqxAuthUser(matches(mqttUsernamePattern));
    verify(emqxApiClient)
        .createEmqxAuthUser(matches(mqttUsernamePattern), passwordCaptor.capture());
    verify(emqxApiClient).listRulesOfAuthUser(matches(mqttUsernamePattern));
    verify(emqxApiClient).deleteRulesOfAuthUser(matches(mqttUsernamePattern));
    verify(emqxApiClient)
        .createRulesForAuthUser(matches(mqttUsernamePattern), authRulesCaptor.capture());

    final var capturedPassword = passwordCaptor.getValue();

    assertThat(capturedPassword).isNotBlank();
    assertDoesNotThrow(() -> UUID.fromString(capturedPassword));

    assertThat(authRulesCaptor.getValue()).hasSize(2);
    assertThat(authRulesCaptor.getValue().get(0).getTopic())
        .isEqualTo("logreposit/users/+/devices/#");
    assertThat(authRulesCaptor.getValue().get(0).getPermission()).isEqualTo(AuthPermission.ALLOW);
    assertThat(authRulesCaptor.getValue().get(0).getAction()).isEqualTo(AuthAction.ALL);
    assertThat(authRulesCaptor.getValue().get(1).getTopic())
        .isEqualTo("logreposit/users/myUserId1/devices/#");
    assertThat(authRulesCaptor.getValue().get(1).getPermission()).isEqualTo(AuthPermission.ALLOW);
    assertThat(authRulesCaptor.getValue().get(1).getAction()).isEqualTo(AuthAction.SUBSCRIBE);

    verify(mqttCredentialRepository).save(any(MqttCredential.class));

    assertThat(createdCredential.getCreatedAt()).isEqualTo(now);
    assertThat(createdCredential.getDescription()).isEqualTo("some descriptional text");
    assertThat(createdCredential.getUserId()).isEqualTo(userId);
    assertThat(createdCredential.getUsername()).matches(mqttUsernamePattern);
    assertThat(createdCredential.getRoles())
        .isEqualTo(List.of(GLOBAL_DEVICE_DATA_WRITE, ACCOUNT_DEVICE_DATA_READ));

    assertDoesNotThrow(() -> UUID.fromString(createdCredential.getId()));
    assertDoesNotThrow(() -> UUID.fromString(createdCredential.getPassword()));
  }

  @Test
  public void testCreate_givenAlreadyExistingEmqxAuthUserWithNotMatchingRules_expectSucceeds() {
    final var userId = "myUserId1";
    final var randomId = UUID.randomUUID().toString();
    final var now = new Date();

    final var mqttUsernamePattern = mqttUsernamePattern(userId);

    when(emqxApiClient.retrieveEmqxAuthUser(matches(mqttUsernamePattern)))
        .thenAnswer(
            i ->
                Optional.of(
                    EmqxAuthUser.builder().userId(i.getArgument(0)).superuser(false).build()));

    when(emqxApiClient.listRulesOfAuthUser(matches(mqttUsernamePattern))).thenReturn(List.of());
    doNothing().when(emqxApiClient).deleteRulesOfAuthUser(matches(mqttUsernamePattern));
    doNothing().when(emqxApiClient).createRulesForAuthUser(matches(mqttUsernamePattern), anyList());

    when(mqttCredentialRepository.save(any(MqttCredential.class)))
        .thenAnswer(
            i -> {
              final var input = i.getArgument(0, MqttCredential.class);

              return MqttCredential.builder()
                  .id(randomId)
                  .createdAt(now)
                  .userId(input.getUserId())
                  .description(input.getDescription())
                  .username(input.getUsername())
                  .password(input.getPassword())
                  .roles(input.getRoles())
                  .build();
            });

    final var createdCredential =
        mqttCredentialService.create(
            userId,
            "some descriptional text",
            List.of(GLOBAL_DEVICE_DATA_WRITE, ACCOUNT_DEVICE_DATA_READ));

    verify(emqxApiClient).retrieveEmqxAuthUser(matches(mqttUsernamePattern));
    verify(emqxApiClient, never()).createEmqxAuthUser(anyString(), anyString());
    verify(emqxApiClient).listRulesOfAuthUser(matches(mqttUsernamePattern));
    verify(emqxApiClient).deleteRulesOfAuthUser(matches(mqttUsernamePattern));
    verify(emqxApiClient)
        .createRulesForAuthUser(matches(mqttUsernamePattern), authRulesCaptor.capture());

    assertThat(authRulesCaptor.getValue()).hasSize(2);
    assertThat(authRulesCaptor.getValue().get(0).getTopic())
        .isEqualTo("logreposit/users/+/devices/#");
    assertThat(authRulesCaptor.getValue().get(0).getPermission()).isEqualTo(AuthPermission.ALLOW);
    assertThat(authRulesCaptor.getValue().get(0).getAction()).isEqualTo(AuthAction.ALL);
    assertThat(authRulesCaptor.getValue().get(1).getTopic())
        .isEqualTo("logreposit/users/myUserId1/devices/#");
    assertThat(authRulesCaptor.getValue().get(1).getPermission()).isEqualTo(AuthPermission.ALLOW);
    assertThat(authRulesCaptor.getValue().get(1).getAction()).isEqualTo(AuthAction.SUBSCRIBE);

    verify(mqttCredentialRepository).save(any(MqttCredential.class));

    assertThat(createdCredential.getCreatedAt()).isEqualTo(now);
    assertThat(createdCredential.getDescription()).isEqualTo("some descriptional text");
    assertThat(createdCredential.getUserId()).isEqualTo(userId);
    assertThat(createdCredential.getUsername()).matches(mqttUsernamePattern);
    assertThat(createdCredential.getRoles())
        .isEqualTo(List.of(GLOBAL_DEVICE_DATA_WRITE, ACCOUNT_DEVICE_DATA_READ));

    assertDoesNotThrow(() -> UUID.fromString(createdCredential.getId()));
    assertDoesNotThrow(() -> UUID.fromString(createdCredential.getPassword()));
  }

  @Test
  public void testCreate_givenAlreadyExistingEmqxAuthUserWithMatchingRules_expectSucceeds() {
    final var userId = "myUserId1";
    final var randomId = UUID.randomUUID().toString();
    final var now = new Date();

    final var mqttUsernamePattern = mqttUsernamePattern(userId);

    when(emqxApiClient.retrieveEmqxAuthUser(matches(mqttUsernamePattern)))
        .thenAnswer(
            i ->
                Optional.of(
                    EmqxAuthUser.builder().userId(i.getArgument(0)).superuser(false).build()));

    when(emqxApiClient.listRulesOfAuthUser(matches(mqttUsernamePattern)))
        .thenReturn(
            List.of(
                EmqxAuthRule.builder()
                    .topic("logreposit/users/+/devices/#")
                    .permission(AuthPermission.ALLOW)
                    .action(AuthAction.ALL)
                    .build(),
                EmqxAuthRule.builder()
                    .topic("logreposit/users/myUserId1/devices/#")
                    .permission(AuthPermission.ALLOW)
                    .action(AuthAction.SUBSCRIBE)
                    .build()));

    when(mqttCredentialRepository.save(any(MqttCredential.class)))
        .thenAnswer(
            i -> {
              final var input = i.getArgument(0, MqttCredential.class);

              return MqttCredential.builder()
                  .id(randomId)
                  .createdAt(now)
                  .userId(input.getUserId())
                  .description(input.getDescription())
                  .username(input.getUsername())
                  .password(input.getPassword())
                  .roles(input.getRoles())
                  .build();
            });

    final var createdCredential =
        mqttCredentialService.create(
            userId,
            "some descriptional text",
            List.of(GLOBAL_DEVICE_DATA_WRITE, ACCOUNT_DEVICE_DATA_READ));

    verify(emqxApiClient).retrieveEmqxAuthUser(matches(mqttUsernamePattern));
    verify(emqxApiClient, never()).createEmqxAuthUser(anyString(), anyString());
    verify(emqxApiClient).listRulesOfAuthUser(matches(mqttUsernamePattern));
    verify(emqxApiClient, never()).deleteRulesOfAuthUser(anyString());
    verify(emqxApiClient, never()).createRulesForAuthUser(anyString(), anyList());

    verify(mqttCredentialRepository).save(any(MqttCredential.class));

    assertThat(createdCredential.getCreatedAt()).isEqualTo(now);
    assertThat(createdCredential.getDescription()).isEqualTo("some descriptional text");
    assertThat(createdCredential.getUserId()).isEqualTo(userId);
    assertThat(createdCredential.getUsername()).matches(mqttUsernamePattern);
    assertThat(createdCredential.getRoles())
        .isEqualTo(List.of(GLOBAL_DEVICE_DATA_WRITE, ACCOUNT_DEVICE_DATA_READ));

    assertDoesNotThrow(() -> UUID.fromString(createdCredential.getId()));
    assertDoesNotThrow(() -> UUID.fromString(createdCredential.getPassword()));
  }

  @Test
  public void testList_givenSavedMqttCredentials_expectReturnsPage() {
    final var userId = "myUserId";

    final var savedCredential1 = savedCredential(1, userId, List.of(ACCOUNT_DEVICE_DATA_READ));
    final var savedCredential2 = savedCredential(2, userId, List.of(ACCOUNT_DEVICE_DATA_READ));

    final var savedMqttCredentials = List.of(savedCredential1, savedCredential2);

    final var pageResponse = new PageImpl<>(savedMqttCredentials);

    when(mqttCredentialRepository.findByUserId(eq(userId), any())).thenReturn(pageResponse);

    final var page = mqttCredentialService.list(userId, 1, 10);

    verify(mqttCredentialRepository).findByUserId(eq(userId), any());

    final var items = page.stream().toList();

    assertThat(items).hasSize(2);
    assertThat(items.get(0).getUsername()).isEqualTo("some_user_1");
    assertThat(items.get(1).getUsername()).isEqualTo("some_user_2");
  }

  @Test
  public void testGet_givenSavedMqttCredential_expectReturnsItem() {
    final var userId = "myUserId";

    final var savedCredential = savedCredential(42, userId, List.of(ACCOUNT_DEVICE_DATA_READ));

    when(mqttCredentialRepository.findByIdAndUserId(eq(savedCredential.getId()), eq(userId)))
        .thenReturn(Optional.of(savedCredential));

    final var retrievedCredential = mqttCredentialService.get(savedCredential.getId(), userId);

    verify(mqttCredentialRepository).findByIdAndUserId(eq(savedCredential.getId()), eq(userId));

    assertThat(retrievedCredential.getUsername()).isEqualTo("some_user_42");
  }

  @Test
  public void testGet_givenNotSavedMqttCredential_expectException() {
    final var userId = "myUserId";
    final var credentialId = "myCredentialId";

    when(mqttCredentialRepository.findByIdAndUserId(eq(credentialId), eq(userId)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> mqttCredentialService.get(credentialId, userId))
        .isExactlyInstanceOf(MqttCredentialNotFoundException.class)
        .hasMessage("could not find mqtt credential with id");

    verify(mqttCredentialRepository).findByIdAndUserId(eq(credentialId), eq(userId));
  }

  @Test
  public void testGetGlobalDeviceDataWriteCredential_givenExistent_expectReturnsSuccess() {
    final var savedCredential =
        savedCredential(43, "myWriteUserId", List.of(GLOBAL_DEVICE_DATA_WRITE));

    when(this.mqttCredentialRepository.findFirstByRolesContaining(eq(GLOBAL_DEVICE_DATA_WRITE)))
        .thenReturn(Optional.of(savedCredential));

    final var retrievedCredential = mqttCredentialService.getGlobalDeviceDataWriteCredential();

    verify(this.mqttCredentialRepository).findFirstByRolesContaining(eq(GLOBAL_DEVICE_DATA_WRITE));

    assertThat(retrievedCredential.getUsername()).isEqualTo("some_user_43");
  }

  @Test
  public void testGetGlobalDeviceDataWriteCredential_givenNotExistent_expectReturnsException() {
    when(this.mqttCredentialRepository.findFirstByRolesContaining(eq(GLOBAL_DEVICE_DATA_WRITE)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> mqttCredentialService.getGlobalDeviceDataWriteCredential())
        .isExactlyInstanceOf(MqttCredentialNotFoundException.class)
        .hasMessage("could not find mqtt credential with role GLOBAL_DEVICE_DATA_WRITE");

    verify(this.mqttCredentialRepository).findFirstByRolesContaining(eq(GLOBAL_DEVICE_DATA_WRITE));
  }

  /*
   *
   * MqttCredential delete(String mqttCredentialId, String userId)
   * void syncAll()
   * sync(MqttCredential mqttCredential)
   */

  private Pattern mqttUsernamePattern(String userId) {
    return Pattern.compile(String.format("^mqtt_%s_[0-9a-zA-z]{5}$", userId));
  }

  private MqttCredential savedCredential(int number, String userId, List<MqttRole> roles) {
    return MqttCredential.builder()
        .id(UUID.randomUUID().toString())
        .userId(userId)
        .createdAt(new Date())
        .description("Saved credential")
        .username("some_user_" + number)
        .password("some_password_" + number)
        .roles(roles)
        .build();
  }
}
