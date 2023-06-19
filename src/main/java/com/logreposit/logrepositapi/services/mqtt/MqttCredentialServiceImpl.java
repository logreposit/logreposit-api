package com.logreposit.logrepositapi.services.mqtt;

import com.logreposit.logrepositapi.configuration.MqttConfiguration;
import com.logreposit.logrepositapi.persistence.documents.MqttCredential;
import com.logreposit.logrepositapi.persistence.documents.MqttRole;
import com.logreposit.logrepositapi.persistence.repositories.MqttCredentialRepository;
import com.logreposit.logrepositapi.services.mqtt.emqx.EmqxApiClient;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.AuthAction;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.AuthPermission;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.EmqxAuthRule;
import com.logreposit.logrepositapi.services.mqtt.emqx.dtos.EmqxAuthUser;
import com.logreposit.logrepositapi.services.mqtt.mosquitto.dynsec.MosquittoDynSecClient;
import com.logreposit.logrepositapi.services.mqtt.mosquitto.dynsec.control.MosquittoDynSecCommandResult;
import com.logreposit.logrepositapi.services.mqtt.mosquitto.dynsec.control.commands.AddClientRoleCommand;
import com.logreposit.logrepositapi.services.mqtt.mosquitto.dynsec.control.commands.AddRoleAclCommand;
import com.logreposit.logrepositapi.services.mqtt.mosquitto.dynsec.control.commands.CreateClientCommand;
import com.logreposit.logrepositapi.services.mqtt.mosquitto.dynsec.control.commands.CreateRoleCommand;
import com.logreposit.logrepositapi.services.mqtt.mosquitto.dynsec.control.commands.DeleteClientCommand;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;
import org.apache.commons.collections4.CollectionUtils;
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

  private final MqttConfiguration mqttConfiguration;
  private final MqttCredentialRepository mqttCredentialRepository;
  private final MosquittoDynSecClient mosquittoDynSecClient;
  private final EmqxApiClient emqxApiClient;

  public MqttCredentialServiceImpl(
      MqttConfiguration mqttConfiguration,
      MqttCredentialRepository mqttCredentialRepository,
      MosquittoDynSecClient mosquittoDynSecClient,
      EmqxApiClient emqxApiClient) {
    this.mqttConfiguration = mqttConfiguration;
    this.mqttCredentialRepository = mqttCredentialRepository;
    this.mosquittoDynSecClient = mosquittoDynSecClient;
    this.emqxApiClient = emqxApiClient;
  }

  @Override
  public MqttCredential create(String userId, String description, List<MqttRole> roles)
      throws MqttCredentialServiceException {
    final var mqttCredential = new MqttCredential();

    mqttCredential.setCreatedAt(new Date());
    mqttCredential.setUserId(userId);
    mqttCredential.setDescription(description);
    mqttCredential.setUsername(generateMqttUsername(userId));
    mqttCredential.setPassword(UUID.randomUUID().toString());
    mqttCredential.setRoles(roles);

    // TODO: Separate DB entry initialization & Broker user initialization
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
    final var mqttCredential = this.get(mqttCredentialId, userId);

    deleteMqttCredentialAtBroker(mqttCredential.getUsername());

    this.mqttCredentialRepository.delete(mqttCredential);

    return mqttCredential;
  }

  @Override
  public MqttCredential getGlobalDeviceDataWriteCredential() {
    final var mqttCredential =
        this.mqttCredentialRepository.findFirstByRolesContaining(MqttRole.GLOBAL_DEVICE_DATA_WRITE);

    if (mqttCredential.isEmpty()) {
      throw new MqttCredentialNotFoundException(
          "could not find mqtt credential with role GLOBAL_DEVICE_DATA_WRITE");
    }

    return mqttCredential.get();
  }

  @Override
  public void sync() {
    try (final var credentials = mqttCredentialRepository.findAllBy()) {
      credentials.forEach(this::syncMqttCredential);
    }
  }

  private void createMqttCredentialAtBroker(MqttCredential mqttCredential)
      throws MqttCredentialServiceException {
    switch (mqttConfiguration.getBrokerType()) {
      case EMQX -> createEmqxMqttCredentialAtBroker(mqttCredential);
      case MOSQUITTO -> createMosquittoMqttCredentialAtBroker(mqttCredential);
    }
  }

  private void syncMqttCredential(MqttCredential mqttCredential) {
    logger.info("Syncing MQTT credential to EMQX Broker: {}", mqttCredential);

    final var emqxAuthUser = retrieveOrCreateEmqxAuthUser(mqttCredential);

    final var expectedRules =
        mqttCredential.getRoles().stream()
            .map(
                r ->
                    switch (r) {
                      case ACCOUNT_DEVICE_DATA_READ -> accountDeviceDataReadRule(mqttCredential);
                      case GLOBAL_DEVICE_DATA_WRITE -> globalDeviceDataWriteRule();
                    })
            .toList();

    final var existingRules = this.emqxApiClient.listRulesOfAuthUser(mqttCredential.getUsername());

    if (CollectionUtils.isEqualCollection(existingRules, expectedRules)) {
      logger.info("Already existing rules do match the expected set of rules.");

      return;
    }

    logger.info("Rules are not up2date on the EMQX broker yet. Setting them up correctly ...");

    this.emqxApiClient.deleteRulesOfAuthUser(mqttCredential.getUsername());
    this.emqxApiClient.createRulesForAuthUser(mqttCredential.getUsername(), expectedRules);

    logger.info(
        "Successfully created {} rules for AuthUser '{}'!",
        expectedRules.size(),
        emqxAuthUser.getUserId());
  }

  private EmqxAuthUser retrieveOrCreateEmqxAuthUser(MqttCredential mqttCredential) {
    logger.info(
        "Checking if AuthUser with username '{}' is already existent on EMQX broker ...",
        mqttCredential.getUsername());

    final var emqxAuthUserOptional =
        emqxApiClient.retrieveEmqxAuthUser(mqttCredential.getUsername());

    if (emqxAuthUserOptional.isPresent()) {
      logger.info("EMQX Auth User '{}' already exists.", mqttCredential.getUsername());

      return emqxAuthUserOptional.get();
    }

    logger.info(
        "EMQX Auth User '{}' does not exist yet, creating it ...", mqttCredential.getUsername());

    return emqxApiClient.createEmqxAuthUser(
        mqttCredential.getUsername(), mqttCredential.getPassword());
  }

  private String generateMqttUsername(String userId) {
    final var randomPart = RandomStringUtils.random(5, true, true).toLowerCase(Locale.US);

    return String.format("mqtt_%s_%s", userId, randomPart);
  }

  private void createEmqxMqttCredentialAtBroker(MqttCredential mqttCredential) {
    syncMqttCredential(mqttCredential);
  }

  private EmqxAuthRule globalDeviceDataWriteRule() {
    return EmqxAuthRule.builder()
        .topic("logreposit/users/+/devices/#")
        .permission(AuthPermission.ALLOW)
        .action(AuthAction.ALL)
        .build();
  }

  private EmqxAuthRule accountDeviceDataReadRule(MqttCredential mqttCredential) {
    final var topicPattern =
        String.format("logreposit/users/%s/devices/#", mqttCredential.getUserId());

    return EmqxAuthRule.builder()
        .topic(topicPattern)
        .permission(AuthPermission.ALLOW)
        .action(AuthAction.SUBSCRIBE)
        .build();
  }

  private void createMosquittoMqttCredentialAtBroker(MqttCredential mqttCredential) {
    final var userId = mqttCredential.getUserId();
    final var username = mqttCredential.getUsername();

    createMosquittoMqttRolesIgnoringAlreadyExistent(userId, mqttCredential.getRoles());

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

  private void createMosquittoMqttRolesIgnoringAlreadyExistent(
      String userId, List<MqttRole> roles) {
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
    mosquittoDynSecClient
        .sendCommands(List.of(new DeleteClientCommand(username)))
        .forEach(this::validateDynSecCommandResultIgnoringAlreadyExistantRolesAndACLs);
  }

  private void validateDynSecCommandResultIgnoringAlreadyExistantRolesAndACLs(
      MosquittoDynSecCommandResult result) throws MqttCredentialServiceException {
    final var response = result.getResponse();
    final var command = result.getCommand();

    if (response.getError() == null) {
      logger.info(
          "MQTT command '{}' (correlationData='{}') => OK",
          result.getCommand().getCommand(),
          result.getCommand().getCorrelationData());

      return;
    }

    final var error = response.getError();

    if (command instanceof CreateRoleCommand && error.endsWith("already exists")) {
      logger.info(
          "MQTT command '{}' (correlationData='{}') => '{}' => OK",
          command.getCommand(),
          command.getCorrelationData(),
          error);

      return;
    }

    if (command instanceof AddRoleAclCommand && error.endsWith("already exists")) {
      logger.info(
          "MQTT command '{}' (correlationData='{}') => '{}' => OK",
          command.getCommand(),
          command.getCorrelationData(),
          error);

      return;
    }

    logger.error("Unable to perform Mosquitto DynSec command {}: {}", command, error);

    throw new MqttCredentialServiceException(
        String.format("Unable to perform Mosquitto DynSec command: %s", response));
  }
}
