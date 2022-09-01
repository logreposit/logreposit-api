package com.logreposit.logrepositapi.services.mqtt;

import com.logreposit.logrepositapi.communication.messaging.mqtt.MosquittoDynSecClient;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.AddClientRoleCommand;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.AddRoleAclCommand;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.CreateClientCommand;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.CreateRoleCommand;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.DeleteClientCommand;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.MosquittoControlApiResponse;
import com.logreposit.logrepositapi.persistence.documents.MqttCredential;
import com.logreposit.logrepositapi.persistence.documents.MqttRole;
import com.logreposit.logrepositapi.persistence.repositories.MqttCredentialRepository;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class MqttCredentialServiceImpl implements MqttCredentialService {
  private static final Logger logger = LoggerFactory.getLogger(MqttCredentialServiceImpl.class);

  private final MqttCredentialRepository mqttCredentialRepository;
  private final MosquittoDynSecClient mosquittoDynSecClient;

  public MqttCredentialServiceImpl(
      MqttCredentialRepository mqttCredentialRepository,
      Optional<MosquittoDynSecClient> mosquittoDynSecClientOptional) {
    this.mqttCredentialRepository = mqttCredentialRepository;
    this.mosquittoDynSecClient = mosquittoDynSecClientOptional.orElse(null);
  }

  @Override
  public MqttCredential create(String userId, String description) throws MqttServiceException {
    checkIfMqttIsSupported();

    final var mqttCredential = new MqttCredential();

    mqttCredential.setCreatedAt(new Date());
    mqttCredential.setUserId(userId);
    mqttCredential.setDescription(description);
    mqttCredential.setUsername(generateMqttUsername(userId));
    mqttCredential.setPassword(UUID.randomUUID().toString());
    // `roles` currently hardcoded to [DEVICE_DATA_READ], nothing else supported yet.
    mqttCredential.setRoles(List.of(MqttRole.DEVICE_DATA_READ));

    createMqttCredentialAtBroker(mqttCredential);

    final var createdMqttCredential = this.mqttCredentialRepository.save(mqttCredential);

    logger.info("Successfully created new mqtt credential: {}", createdMqttCredential);

    return createdMqttCredential;
  }

  @Override
  public Page<MqttCredential> list(String userId, Integer page, Integer size) {
    final var pageRequest = PageRequest.of(page, size);

    return this.mqttCredentialRepository.findByUserId(userId, pageRequest);
  }

  @Override
  public MqttCredential get(String mqttCredentialId, String userId)
      throws MqttCredentialNotFoundException {
    final var mqttCredential =
        this.mqttCredentialRepository.findByIdAndUserId(mqttCredentialId, userId);

    if (mqttCredential.isEmpty()) {
      logger.error("could not find mqtt credential with id {}.", mqttCredentialId);

      throw new MqttCredentialNotFoundException("could not find mqtt credential with id");
    }

    return mqttCredential.get();
  }

  @Override
  public MqttCredential delete(String mqttCredentialId, String userId) throws MqttServiceException {
    checkIfMqttIsSupported();

    final var mqttCredential = this.get(mqttCredentialId, userId);

    deleteMqttCredentialAtBroker(mqttCredential.getUsername());

    this.mqttCredentialRepository.delete(mqttCredential);

    return mqttCredential;
  }

  private void checkIfMqttIsSupported() throws MqttSupportNotEnabledException {
    if (mosquittoDynSecClient == null) {
      throw new MqttSupportNotEnabledException();
    }
  }

  private String generateMqttUsername(String userId) {
    final var randomPart = RandomStringUtils.random(5, true, true).toLowerCase(Locale.US);

    return String.format("mqtt_%s_%s", userId, randomPart);
  }

  private void createMqttCredentialAtBroker(MqttCredential mqttCredential)
      throws MqttServiceException {
    final var userId = mqttCredential.getUserId();
    final var deviceDataReadRoleName = String.format("deviceDataRead_%s", userId);
    final var topicPattern = String.format("logreposit/users/%s/devices/#", userId);
    final var username = mqttCredential.getUsername();

    final var createRoleCommand = new CreateRoleCommand(deviceDataReadRoleName);
    final var addRoleAclCommand =
        new AddRoleAclCommand(deviceDataReadRoleName, "subscribePattern", topicPattern, true);
    final var createClientCommand = new CreateClientCommand(username, mqttCredential.getPassword());
    final var addClientRoleCommand = new AddClientRoleCommand(username, deviceDataReadRoleName);

    final var responses =
        mosquittoDynSecClient.sendCommands(
            List.of(
                createRoleCommand, addRoleAclCommand, createClientCommand, addClientRoleCommand));

    final var createRoleResponse = getResponse(responses, createRoleCommand.getCorrelationData());

    if (createRoleResponse.getError() != null
        && !createRoleResponse.getError().endsWith("already exists")) {
      throw new MqttServiceException(
          "Unable to create Role at MQTT broker: " + createRoleResponse.getError());
    }

    final var addRoleAclResponse = getResponse(responses, addRoleAclCommand.getCorrelationData());

    if (addRoleAclResponse.getError() != null
        && !addRoleAclResponse.getError().endsWith("already exists")) {
      throw new MqttServiceException(
          "Unable to add ACL to Role at MQTT broker: " + createRoleResponse.getError());
    }

    final var createClientCommandResponse =
        getResponse(responses, createClientCommand.getCorrelationData());

    if (createClientCommandResponse.getError() != null) {
      throw new MqttServiceException(
          "Unable to create MQTT client (username) at MQTT broker: "
              + createRoleResponse.getError());
    }

    final var addClientRoleCommandResponse =
        getResponse(responses, addClientRoleCommand.getCorrelationData());

    if (addClientRoleCommandResponse.getError() != null) {
      throw new MqttServiceException(
          "Unable to add Role to MQTT client (username) at MQTT broker: "
              + createRoleResponse.getError());
    }
  }

  private void deleteMqttCredentialAtBroker(String username) throws MqttServiceException {
    final var deleteClientCommand = new DeleteClientCommand(username);
    final var responses = mosquittoDynSecClient.sendCommands(List.of(deleteClientCommand));
    final var deleteClientResponse =
        getResponse(responses, deleteClientCommand.getCorrelationData());

    if (deleteClientResponse.getError() != null) {
      throw new MqttServiceException(
          "Unable to delete MQTT client (username) at MQTT broker: "
              + deleteClientResponse.getError());
    }
  }

  private MosquittoControlApiResponse getResponse(
      List<MosquittoControlApiResponse> responses, String correlationData) {
    return responses.stream()
        .filter(r -> correlationData.equals(r.getCorrelationData()))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "MQTT responses should have contained a response with given correlationData."));
  }
}
