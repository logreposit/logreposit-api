package com.logreposit.logrepositapi.communication.messaging.mqtt;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logreposit.logrepositapi.configuration.MqttConfiguration;
import com.logreposit.logrepositapi.persistence.documents.MqttCredential;
import com.logreposit.logrepositapi.persistence.documents.MqttRole;
import com.logreposit.logrepositapi.persistence.documents.User;
import com.logreposit.logrepositapi.services.mqtt.MqttCredentialService;
import com.logreposit.logrepositapi.services.user.UserNotFoundException;
import com.logreposit.logrepositapi.services.user.UserService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
public class MqttAutoConfigurationCommandLineRunnerTests {
  private MqttAutoConfigurationCommandLineRunner mqttAutoConfigurationCommandLineRunner;

  @Mock private MqttConfiguration mqttConfiguration;

  @Mock private MqttCredentialService mqttCredentialService;

  @Mock private UserService userService;

  @BeforeEach
  public void setUp() {
    this.mqttAutoConfigurationCommandLineRunner =
        new MqttAutoConfigurationCommandLineRunner(
            mqttConfiguration, mqttCredentialService, userService);
  }

  @Test
  public void testRun_givenNoMqttCredentialExistentYet_expectCreatesOne() throws Exception {
    final var existentApiUser = new User();

    existentApiUser.setId("myAdminApiUserId");

    when(mqttConfiguration.isEnabled()).thenReturn(true);
    when(userService.getFirstAdmin()).thenReturn(existentApiUser);

    when(mqttCredentialService.list(eq(existentApiUser.getId()), eq(0), eq(1)))
        .thenReturn(new PageImpl<>(List.of()));

    when(mqttCredentialService.create(
            eq(existentApiUser.getId()),
            eq("Logreposit API MQTT client credential used to publish device updates"),
            eq(List.of(MqttRole.GLOBAL_DEVICE_DATA_WRITE))))
        .thenReturn(
            MqttCredential.builder()
                .id("myMqttCredentialId")
                .userId(existentApiUser.getId())
                .username("mqttUser")
                .password("mqttPass")
                .roles(List.of(MqttRole.GLOBAL_DEVICE_DATA_WRITE))
                .description("myDescr")
                .build());

    mqttAutoConfigurationCommandLineRunner.run();

    verify(userService).getFirstAdmin();
    verify(mqttCredentialService).list(eq(existentApiUser.getId()), eq(0), eq(1));
    verify(mqttCredentialService)
        .create(
            eq(existentApiUser.getId()),
            eq("Logreposit API MQTT client credential used to publish device updates"),
            eq(List.of(MqttRole.GLOBAL_DEVICE_DATA_WRITE)));
  }

  @Test
  public void testRun_givenNoMqttCredentialExistentYetButMqttSupportIsDisabled_expectSkipsCreation()
      throws Exception {
    final var existentApiUser = new User();

    existentApiUser.setId("myAdminApiUserId");

    when(mqttConfiguration.isEnabled()).thenReturn(false);
    when(userService.getFirstAdmin()).thenReturn(existentApiUser);

    when(mqttCredentialService.list(eq(existentApiUser.getId()), eq(0), eq(1)))
        .thenReturn(new PageImpl<>(List.of()));

    mqttAutoConfigurationCommandLineRunner.run();

    verify(userService).getFirstAdmin();
    verify(mqttCredentialService).list(eq(existentApiUser.getId()), eq(0), eq(1));
    verify(mqttCredentialService, never()).create(anyString(), anyString(), anyList());
  }

  @Test
  public void testRun_givenMqttCredentialAlreadyExistent_expectTriggersSync() throws Exception {
    final var existentApiUser = new User();

    existentApiUser.setId("myAdminApiUserId");

    when(mqttConfiguration.isEnabled()).thenReturn(true);
    when(userService.getFirstAdmin()).thenReturn(existentApiUser);

    final var existingMqttCredential = MqttCredential.builder().id("someId").build();

    when(mqttCredentialService.list(eq(existentApiUser.getId()), eq(0), eq(1)))
        .thenReturn(new PageImpl<>(List.of(existingMqttCredential)));

    doNothing().when(mqttCredentialService).sync(same(existingMqttCredential));

    mqttAutoConfigurationCommandLineRunner.run();

    verify(userService).getFirstAdmin();
    verify(mqttCredentialService).list(eq(existentApiUser.getId()), eq(0), eq(1));
    verify(mqttCredentialService).sync(same(existingMqttCredential));
  }

  @Test
  public void
      testRun_givenMqttCredentialAlreadyExistentButMqttSupportDisabled_expectDoesNotTriggerSync()
          throws Exception {
    final var existentApiUser = new User();

    existentApiUser.setId("myAdminApiUserId");

    when(mqttConfiguration.isEnabled()).thenReturn(false);
    when(userService.getFirstAdmin()).thenReturn(existentApiUser);

    final var existingMqttCredential = MqttCredential.builder().id("someId").build();

    when(mqttCredentialService.list(eq(existentApiUser.getId()), eq(0), eq(1)))
        .thenReturn(new PageImpl<>(List.of(existingMqttCredential)));

    mqttAutoConfigurationCommandLineRunner.run();

    verify(userService).getFirstAdmin();
    verify(mqttCredentialService).list(eq(existentApiUser.getId()), eq(0), eq(1));
    verify(mqttCredentialService, never()).sync(any(MqttCredential.class));
  }

  @Test
  public void testRun_givenNoAdminUserExistent_expectException() throws UserNotFoundException {
    when(userService.getFirstAdmin()).thenThrow(new UserNotFoundException("exception message"));

    assertThatThrownBy(() -> mqttAutoConfigurationCommandLineRunner.run())
        .isExactlyInstanceOf(UserNotFoundException.class)
        .hasMessage("exception message");
  }
}
