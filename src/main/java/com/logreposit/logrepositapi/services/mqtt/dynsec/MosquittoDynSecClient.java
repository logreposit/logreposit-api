package com.logreposit.logrepositapi.services.mqtt.dynsec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.services.mqtt.dynsec.control.MosquittoControlApiCommand;
import com.logreposit.logrepositapi.services.mqtt.dynsec.control.MosquittoControlApiRequest;
import com.logreposit.logrepositapi.services.mqtt.dynsec.control.MosquittoControlApiResponse;
import com.logreposit.logrepositapi.services.mqtt.dynsec.control.MosquittoControlApiResponses;
import com.logreposit.logrepositapi.services.mqtt.dynsec.control.MosquittoDynSecCommandResult;
import java.io.IOException;
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

@Slf4j
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

  // TODO: retries needed??
  public <T extends MosquittoControlApiCommand> List<MosquittoDynSecCommandResult> sendCommands(
      List<T> commands) {
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

    try {
      final var responses = finishedFutures.get(10, TimeUnit.SECONDS);

      return responses.stream()
          .map(
              r ->
                  new MosquittoDynSecCommandResult(getCommand(commands, r.getCorrelationData()), r))
          .toList();
    } catch (ExecutionException | InterruptedException | TimeoutException e) {
      throw new MosquittoDynSecClientException(
          "Error while executing and/or waiting for MQTT command responses.", e);
    }
  }

  private void publishCommands(List<? extends MosquittoControlApiCommand> commands) {
    final var request = MosquittoControlApiRequest.builder().commands(commands).build();

    try {
      final var mqttMessage = mqttMessage(request);

      mqttClient.publish(MOSQUITTO_DYNSEC_TOPIC, mqttMessage);
    } catch (JsonProcessingException e) {
      throw new MosquittoDynSecClientException(
          "Unable to create MQTT message, error while serializing data.", e);
    } catch (MqttException e) {
      throw new MosquittoDynSecClientException("Error while publishing data to MQTT broker", e);
    }
  }

  private MqttMessage mqttMessage(MosquittoControlApiRequest request)
      throws JsonProcessingException {
    final var payload = objectMapper.writeValueAsBytes(request);
    final var message = new MqttMessage();

    message.setQos(MOSQUITTO_DYNSEC_QOS);
    message.setPayload(payload);

    return message;
  }

  private <T extends MosquittoControlApiCommand> MosquittoControlApiCommand getCommand(
      List<T> requests, String correlationData) {
    return requests.stream()
        .filter(r -> correlationData.equals(r.getCorrelationData()))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "List should have contained a command with given correlationData."));
  }
}
