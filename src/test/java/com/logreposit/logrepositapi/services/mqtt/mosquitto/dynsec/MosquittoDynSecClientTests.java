package com.logreposit.logrepositapi.services.mqtt.mosquitto.dynsec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.configuration.MqttConfiguration;
import com.logreposit.logrepositapi.services.mqtt.MqttClientProvider;
import com.logreposit.logrepositapi.services.mqtt.mosquitto.dynsec.control.MosquittoControlApiResponse;
import com.logreposit.logrepositapi.services.mqtt.mosquitto.dynsec.control.MosquittoControlApiResponses;
import com.logreposit.logrepositapi.services.mqtt.mosquitto.dynsec.control.commands.AddClientRoleCommand;
import com.logreposit.logrepositapi.services.mqtt.mosquitto.dynsec.control.commands.AddRoleAclCommand;
import com.logreposit.logrepositapi.services.mqtt.mosquitto.dynsec.control.commands.CreateClientCommand;
import com.logreposit.logrepositapi.services.mqtt.mosquitto.dynsec.control.commands.CreateRoleCommand;
import com.logreposit.logrepositapi.services.mqtt.mosquitto.dynsec.control.commands.DeleteClientCommand;
import com.logreposit.logrepositapi.services.mqtt.mosquitto.dynsec.control.commands.MosquittoControlApiCommand;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
public class MosquittoDynSecClientTests {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock private MqttConfiguration mqttConfiguration;
  @Mock private MqttClientProvider mqttClientProvider;

  @Mock private IMqttClient mqttClient;

  private MosquittoDynSecClient mosquittoDynSecClient;

  @BeforeEach
  public void setUp() {
    this.mosquittoDynSecClient =
        new MosquittoDynSecClient(objectMapper, mqttConfiguration, mqttClientProvider);
  }

  @Test
  public void testSendCreateClientCommand_expectAsyncResponseMessageIsParsedAndReturnedToCaller()
      throws MqttException {
    when(mqttConfiguration.getUsername()).thenReturn("myuser");
    when(mqttConfiguration.getPassword()).thenReturn("mypassword");

    when(mqttClientProvider.getMqttClient(eq("myuser"), eq("mypassword"))).thenReturn(mqttClient);

    Mockito.doAnswer(
            (Answer<Void>)
                invocation -> {
                  IMqttMessageListener listener =
                      invocation.getArgument(2, IMqttMessageListener.class);

                  final var mosquittoResponse = new MosquittoControlApiResponse();

                  mosquittoResponse.setCorrelationData("myCreateUserCorrelationData");
                  mosquittoResponse.setError("myAwesomeError");

                  final var mosquittoResponses = new MosquittoControlApiResponses();

                  mosquittoResponses.setResponses(List.of(mosquittoResponse));

                  final var mqttMessage = new MqttMessage();

                  mqttMessage.setPayload(objectMapper.writeValueAsBytes(mosquittoResponses));

                  listener.messageArrived("topicDoesNotMatter", mqttMessage);

                  return null;
                })
        .when(mqttClient)
        .subscribe(
            eq("$CONTROL/dynamic-security/v1/response"), eq(2), any(IMqttMessageListener.class));

    final var createClientCommand = new CreateClientCommand("myNewUser", "myNewPassword");

    createClientCommand.setCorrelationData("myCreateUserCorrelationData");

    final var results = mosquittoDynSecClient.sendCommands(List.of(createClientCommand));

    Assertions.assertThat(results).hasSize(1);
    Assertions.assertThat(results.get(0).getCommand().getCorrelationData())
        .isEqualTo("myCreateUserCorrelationData");
    Assertions.assertThat(results.get(0).getCommand().getCommand()).isEqualTo("createClient");
    Assertions.assertThat(results.get(0).getResponse().getCorrelationData())
        .isEqualTo("myCreateUserCorrelationData");
    Assertions.assertThat(results.get(0).getResponse().getError()).isEqualTo("myAwesomeError");
  }

  @Test
  public void testSendCommands_expectAsyncResponseMessageIsParsedAndReturnedToCaller()
      throws MqttException {
    final var commands =
        List.of(
            new CreateRoleCommand("myRole"),
            new AddRoleAclCommand("myRole", "subscribePattern", "my/pattern/path/#", true),
            new CreateClientCommand("myNewUsername", "myAwesomePassword"),
            new AddClientRoleCommand("myNewUsername", "myRole"),
            new DeleteClientCommand("myNewUsername"));

    testSendCommands_expectAsyncResponseMessageIsParsedAndReturnedToCaller(commands);
  }

  private <T extends MosquittoControlApiCommand>
      void testSendCommands_expectAsyncResponseMessageIsParsedAndReturnedToCaller(List<T> commands)
          throws MqttException {
    when(mqttConfiguration.getUsername()).thenReturn("myuser");
    when(mqttConfiguration.getPassword()).thenReturn("mypassword");

    when(mqttClientProvider.getMqttClient(eq("myuser"), eq("mypassword"))).thenReturn(mqttClient);

    Mockito.doAnswer(
            (Answer<Void>)
                invocation -> {
                  IMqttMessageListener listener =
                      invocation.getArgument(2, IMqttMessageListener.class);

                  final var responses =
                      commands.stream()
                          .map(
                              r -> {
                                final var response = new MosquittoControlApiResponse();

                                response.setCorrelationData(r.getCorrelationData());
                                response.setCommand(r.getCommand());

                                return response;
                              })
                          .toList();

                  final var mosquittoResponses = new MosquittoControlApiResponses();

                  mosquittoResponses.setResponses(responses);

                  final var mqttMessage = new MqttMessage();

                  mqttMessage.setPayload(objectMapper.writeValueAsBytes(mosquittoResponses));

                  listener.messageArrived("topicDoesNotMatter", mqttMessage);

                  return null;
                })
        .when(mqttClient)
        .subscribe(
            eq("$CONTROL/dynamic-security/v1/response"), eq(2), any(IMqttMessageListener.class));

    final var results = mosquittoDynSecClient.sendCommands(commands);

    Assertions.assertThat(results).hasSize(commands.size());

    commands.forEach(
        initialCommand -> {
          final var matchingResult =
              results.stream()
                  .filter(
                      r ->
                          initialCommand
                              .getCorrelationData()
                              .equals(r.getCommand().getCorrelationData()))
                  .findFirst()
                  .orElseThrow();

          final var resultCommand = matchingResult.getCommand();
          final var resultResponse = matchingResult.getResponse();

          assertThat(initialCommand).isEqualTo(resultCommand);
          assertThat(initialCommand.getCorrelationData())
              .isEqualTo(resultResponse.getCorrelationData());
          Assertions.assertThat(resultResponse.getCommand()).isEqualTo(initialCommand.getCommand());
          Assertions.assertThat(resultResponse.getError())
              .isNull(); // error is not set in the callback above
        });
  }

  @Test
  public void testSendCreateClientCommand_givenNoResponse_expectRunsIntoTimeoutAfter10Seconds()
      throws MqttException {
    when(mqttConfiguration.getUsername()).thenReturn("myuser");
    when(mqttConfiguration.getPassword()).thenReturn("mypassword");

    when(mqttClientProvider.getMqttClient(eq("myuser"), eq("mypassword"))).thenReturn(mqttClient);

    Mockito.doNothing()
        .when(mqttClient)
        .subscribe(
            eq("$CONTROL/dynamic-security/v1/response"), eq(2), any(IMqttMessageListener.class));

    final var createClientCommand = new CreateClientCommand("myNewUser", "myNewPassword");

    final var startedAt = Instant.now();

    assertThatThrownBy(() -> mosquittoDynSecClient.sendCommands(List.of(createClientCommand)))
        .isExactlyInstanceOf(MosquittoDynSecClientException.class)
        .hasMessage("Error while executing and/or waiting for MQTT command responses.");

    final var now = Instant.now();

    assertThat(Duration.between(startedAt, now))
        .isCloseTo(Duration.ofSeconds(10), Duration.ofSeconds(1));
  }

  @Test
  public void testSendCreateClientCommand_givenErrorOnPublish_expectThrowsException()
      throws MqttException {
    when(mqttConfiguration.getUsername()).thenReturn("myuser");
    when(mqttConfiguration.getPassword()).thenReturn("mypassword");
    when(mqttClientProvider.getMqttClient(eq("myuser"), eq("mypassword"))).thenReturn(mqttClient);

    doThrow(new RuntimeException("something went wrong"))
        .when(mqttClient)
        .publish(eq("$CONTROL/dynamic-security/v1"), any(MqttMessage.class));

    final var createClientCommand = new CreateClientCommand("myNewUser", "myNewPassword");

    assertThatThrownBy(() -> mosquittoDynSecClient.sendCommands(List.of(createClientCommand)))
        .isExactlyInstanceOf(MosquittoDynSecClientException.class)
        .hasMessage("Error while publishing commands to MQTT broker")
        .hasCauseInstanceOf(RuntimeException.class)
        .hasRootCauseMessage("something went wrong");
  }

  @Test
  public void testSendCreateClientCommand_givenMqttClientProviderReturnsNull_expectThrowsException()
      throws MqttException {
    when(mqttConfiguration.getUsername()).thenReturn("myuser");
    when(mqttConfiguration.getPassword()).thenReturn("mypassword");

    when(mqttClientProvider.getMqttClient(eq("myuser"), eq("mypassword"))).thenReturn(null);

    final var createClientCommand = new CreateClientCommand("myNewUser", "myNewPassword");

    assertThatThrownBy(() -> mosquittoDynSecClient.sendCommands(List.of(createClientCommand)))
        .isExactlyInstanceOf(MosquittoDynSecClientException.class)
        .hasMessage("Error while publishing commands to MQTT broker")
        .hasCauseInstanceOf(RuntimeException.class)
        .hasRootCauseMessage("dynSecMqttClient must not be null!");
  }
}
