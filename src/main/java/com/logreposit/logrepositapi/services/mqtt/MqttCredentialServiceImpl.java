package com.logreposit.logrepositapi.services.mqtt;

import com.logreposit.logrepositapi.communication.messaging.mqtt.MosquittoDynSecClient;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.AddClientRoleCommand;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.AddRoleAclCommand;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.CreateClientCommand;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.CreateRoleCommand;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.DeleteClientCommand;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.MosquittoControlApiResponse;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.MosquittoDynSecCommandResult;
import com.logreposit.logrepositapi.persistence.documents.MqttCredential;
import com.logreposit.logrepositapi.persistence.documents.MqttRole;
import com.logreposit.logrepositapi.persistence.repositories.MqttCredentialRepository;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class MqttCredentialServiceImpl implements MqttCredentialService {
  private static final Logger logger = LoggerFactory.getLogger(MqttCredentialServiceImpl.class);

  private static final String GLOBAL_DEVICE_DATA_WRITE_ROLE_NAME = "globalDeviceDataWrite";

  private final MqttCredentialRepository mqttCredentialRepository;
  private final MosquittoDynSecClient mosquittoDynSecClient;

  public MqttCredentialServiceImpl(
      MqttCredentialRepository mqttCredentialRepository,
      Optional<MosquittoDynSecClient> mosquittoDynSecClientOptional) {
    this.mqttCredentialRepository = mqttCredentialRepository;
    this.mosquittoDynSecClient = mosquittoDynSecClientOptional.orElse(null);
  }

  @Override
  public MqttCredential create(String userId, String description, List<MqttRole> roles)
      throws MqttCredentialServiceException {
    checkIfMqttIsSupported();

    final var mqttCredential = new MqttCredential();

    mqttCredential.setCreatedAt(new Date());
    mqttCredential.setUserId(userId);
    mqttCredential.setDescription(description);
    mqttCredential.setUsername(generateMqttUsername(userId));
    mqttCredential.setPassword(UUID.randomUUID().toString());
    mqttCredential.setRoles(roles);

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
  public MqttCredential delete(String mqttCredentialId, String userId)
      throws MqttCredentialServiceException {
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
      throws MqttCredentialServiceException {
    final var userId = mqttCredential.getUserId();
    final var username = mqttCredential.getUsername();

    createRolesIgnoringAlreadyExistent(userId, mqttCredential.getRoles());

    final var addClientRoleCommands =
        mqttCredential.getRoles().stream()
            .map(
                r ->
                    switch (r) {
                      case ACCOUNT_DEVICE_DATA_READ -> getAccountDeviceDataReadRoleName(userId);
                      case GLOBAL_DEVICE_DATA_WRITE -> GLOBAL_DEVICE_DATA_WRITE_ROLE_NAME;
                    })
            .map(r -> new AddClientRoleCommand(username, r))
            .toList();

    final var createClientAndAssignRoleCommands =
        Stream.concat(
                Stream.of(new CreateClientCommand(username, mqttCredential.getPassword())),
                addClientRoleCommands.stream())
            .toList();

    mosquittoDynSecClient
        .sendCommands(createClientAndAssignRoleCommands)
        .forEach(this::validateDynSecCommandResultIgnoringAlreadyExistantRolesAndACLs);
  }

  private void createRolesIgnoringAlreadyExistent(String userId, List<MqttRole> roles) {
    final var roleCommands =
        roles.stream()
            .map(
                r ->
                    switch (r) {
                      case ACCOUNT_DEVICE_DATA_READ -> {
                        final var roleName = getAccountDeviceDataReadRoleName(userId);
                        final var topicPattern =
                            String.format("logreposit/users/%s/devices/#", userId);

                        final var createRoleCommand = new CreateRoleCommand(roleName);
                        final var addRoleAclCommand =
                            new AddRoleAclCommand(roleName, "subscribePattern", topicPattern, true);

                        yield List.of(createRoleCommand, addRoleAclCommand);
                      }
                      case GLOBAL_DEVICE_DATA_WRITE -> {
                        final var roleName = GLOBAL_DEVICE_DATA_WRITE_ROLE_NAME;
                        final var topicPattern = "logreposit/users/+/devices/#";

                        final var createRoleCommand = new CreateRoleCommand(roleName);
                        final var addRoleAclCommand =
                            new AddRoleAclCommand(
                                roleName, "publishClientSend", topicPattern, true);

                        yield List.of(createRoleCommand, addRoleAclCommand);
                      }
                    })
            .flatMap(Collection::stream)
            .toList();

    mosquittoDynSecClient
        .sendCommands(roleCommands)
        .forEach(this::validateDynSecCommandResultIgnoringAlreadyExistantRolesAndACLs);
  }

  private String getAccountDeviceDataReadRoleName(String userId) {
    return String.format("accountDeviceDataRead_%s", userId);
  }

  private void deleteMqttCredentialAtBroker(String username) throws MqttCredentialServiceException {
    final var deleteClientCommand = new DeleteClientCommand(username);
    final var responses = mosquittoDynSecClient.sendCommands(List.of(deleteClientCommand));
    final var deleteClientResponse =
        getResponse(responses, deleteClientCommand.getCorrelationData());

    if (deleteClientResponse.getError() != null) {
      throw new MqttCredentialServiceException(
          "Unable to delete MQTT client (username) at MQTT broker: "
              + deleteClientResponse.getError());
    }
  }

  private void validateDynSecCommandResultIgnoringAlreadyExistantRolesAndACLs(
      MosquittoDynSecCommandResult result) throws MqttCredentialServiceException {
    final var response = result.getResponse();

    if (response.getError() == null) {
      return;
    }

    final var error = response.getError();
    final var command = result.getCommand();

    if (command instanceof CreateRoleCommand && !error.endsWith("already exists")) {
      throw new MqttCredentialServiceException(
          "Unable to create Role at MQTT broker: " + response.getError());
    }

    if (command instanceof AddRoleAclCommand && !error.endsWith("already exists")) {
      throw new MqttCredentialServiceException(
          "Unable to add ACL to Role at MQTT broker: " + response.getError());
    }

    logger.error("Unable to perform Mosquitto DynSec command {}: {}", command, error);

    throw new MqttCredentialServiceException(
        String.format("Unable to perform Mosquitto DynSec command: %s", response));
  }

  private MosquittoControlApiResponse getResponse(
      List<MosquittoDynSecCommandResult> results, String correlationData) {
    return results.stream()
        .map(MosquittoDynSecCommandResult::getResponse)
        .filter(r -> correlationData.equals(r.getCorrelationData()))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "MQTT responses should have contained a response with given correlationData."));
  }
}
