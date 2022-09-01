package com.logreposit.logrepositapi.communication.messaging.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.AddClientRoleCommand;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.AddRoleAclCommand;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.CreateClientCommand;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.CreateRoleCommand;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.MosquittoControlApiCommand;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.MosquittoControlApiRequest;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.MosquittoControlApiResponse;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.MosquittoControlApiResponses;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnBean(IMqttClient.class)
public class MosquittoDynSecClient {
  private static final String MOSQUITTO_DYNSEC_TOPIC = "$CONTROL/dynamic-security/v1";
  private static final String MOSQUITTO_DYNSEC_RESPONSE_TOPIC =
      MOSQUITTO_DYNSEC_TOPIC + "/response";
  private static final int MOSQUITTO_DYNSEC_QOS = 2;

  private final ObjectMapper objectMapper;
  private final IMqttClient mqttClient;

  private final Map<String, CompletableFuture<MosquittoControlApiResponse>> futures;

  public MosquittoDynSecClient(ObjectMapper objectMapper, IMqttClient mqttClient)
      throws MqttException {
    this.objectMapper = objectMapper;
    this.mqttClient = mqttClient;

    this.futures = new ConcurrentHashMap<>();

    subscribeToControlResponses();
  }

  private void subscribeToControlResponses() throws MqttException {
    mqttClient.subscribe(
        MOSQUITTO_DYNSEC_RESPONSE_TOPIC,
        MOSQUITTO_DYNSEC_QOS,
        (topic, msg) -> {
          final var responses = parseResponses(msg.getPayload());

          final var responsesWithCorrelationData =
              responses.stream()
                  .filter(r -> StringUtils.isNotBlank(r.getCorrelationData()))
                  .toList();

          log.info("Got {} responses with correlation data.", responsesWithCorrelationData.size());

          responsesWithCorrelationData.forEach(
              r -> {
                final var correlationData = r.getCorrelationData();
                final var future = futures.get(correlationData);

                if (future != null) {
                  future.complete(r);
                  futures.remove(correlationData);
                }
              });
        });
  }

  private List<MosquittoControlApiResponse> parseResponses(byte[] payload) throws IOException {
    if (payload.length < 1) {
      return List.of();
    }

    final var responsesWrapper =
        objectMapper.readValue(payload, MosquittoControlApiResponses.class);

    final var responses = responsesWrapper.getResponses();

    log.info("Got {} Mosquitto DynSec responses at once: {}", responses.size(), responses);

    return responses;
  }

  public void createRole(String roleName, String textName, String textDescription)
      throws MqttException, JsonProcessingException, ExecutionException, InterruptedException,
          TimeoutException {
    final var response = sendCommand(new CreateRoleCommand(roleName, roleName, roleName));

    if (response.getError() != null) {
      throw new MosquittoDynSecClientException(
          String.format(
              "Unable to create new Role with name '%s' ('%s', '%s') on Mosquitto MQTT broker: '%s'",
              roleName, textName, textDescription, response.getError()));
    }
  }

  public void addRoleAcl(String roleName, String aclType, String topic, boolean allow)
      throws MqttException, JsonProcessingException, ExecutionException, InterruptedException,
          TimeoutException {
    final var response = sendCommand(new AddRoleAclCommand(roleName, aclType, topic, allow));

    if (response.getError() != null) {
      throw new MosquittoDynSecClientException(
          String.format(
              "Unable to add ACL ('%s', '%s', 'allow=%b') to Role with name '%s' on Mosquitto MQTT broker: '%s'",
              aclType, topic, allow, roleName, response.getError()));
    }
  }

  public void createClient(String username, String password)
      throws MqttException, JsonProcessingException, ExecutionException, InterruptedException,
          TimeoutException {
    final var response = sendCommand(new CreateClientCommand(username, password));

    if (response.getError() != null) {
      throw new MosquittoDynSecClientException(
          String.format(
              "Unable to create new Client with username '%s' on Mosquitto MQTT broker: '%s'",
              username, response.getError()));
    }
  }

  public void addClientRole(String username, String roleName)
      throws MqttException, JsonProcessingException, ExecutionException, InterruptedException,
          TimeoutException {
    final var response = sendCommand(new AddClientRoleCommand(username, roleName));

    if (response.getError() != null) {
      throw new MosquittoDynSecClientException(
          String.format(
              "Unable to add Role with name '%s' to Client with username '%s' on Mosquitto MQTT broker: '%s'",
              roleName, username, response.getError()));
    }
  }

  public <T extends MosquittoControlApiCommand> MosquittoControlApiResponse sendCommand(T command)
      throws MqttException, JsonProcessingException, ExecutionException, InterruptedException,
          TimeoutException {
    final var responses = sendCommands(List.of(command));

    if (responses.size() != 1) {
      throw new IllegalStateException(
          String.format("Expected exactly one response but got %d.", responses.size()));
    }

    return responses.get(0);
  }

  // TODO: retries needed??
  public <T extends MosquittoControlApiCommand> List<MosquittoControlApiResponse> sendCommands(
      List<T> commands)
      throws MqttException, JsonProcessingException, ExecutionException, InterruptedException,
          TimeoutException {
    final var commandFuturesByCorrelationData =
        commands.stream()
            .collect(
                Collectors.toMap(
                    MosquittoControlApiCommand::getCorrelationData,
                    c -> new CompletableFuture<MosquittoControlApiResponse>()));

    futures.putAll(commandFuturesByCorrelationData);

    final var commandFutures = commandFuturesByCorrelationData.values();

    publishCommands(commands);

    final var finishedFutures =
        CompletableFuture.allOf(commandFutures.toArray(new CompletableFuture[0]))
            .thenApply(t -> commandFutures.stream().map(CompletableFuture::join).toList());
    final var responses = finishedFutures.get(10, TimeUnit.SECONDS);

    log.info("Responses: {}", responses);

    return responses;
  }

  private void publishCommands(List<? extends MosquittoControlApiCommand> commands)
      throws JsonProcessingException, MqttException {
    final var request = MosquittoControlApiRequest.builder().commands(commands).build();

    final var mqttMessage = mqttMessage(request);

    mqttClient.publish(MOSQUITTO_DYNSEC_TOPIC, mqttMessage);
  }

  private MqttMessage mqttMessage(MosquittoControlApiRequest request)
      throws JsonProcessingException {
    final var payload = objectMapper.writeValueAsString(request);
    final var message = new MqttMessage();

    message.setQos(MOSQUITTO_DYNSEC_QOS);
    message.setPayload(payload.getBytes(StandardCharsets.UTF_8));

    return message;
  }
}
