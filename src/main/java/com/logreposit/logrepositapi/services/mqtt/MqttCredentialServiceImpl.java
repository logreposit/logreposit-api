package com.logreposit.logrepositapi.services.mqtt;

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
import java.util.Locale;
import java.util.UUID;
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

  private final MqttCredentialRepository mqttCredentialRepository;
  private final EmqxApiClient emqxApiClient;

  public MqttCredentialServiceImpl(
      MqttCredentialRepository mqttCredentialRepository, EmqxApiClient emqxApiClient) {
    this.mqttCredentialRepository = mqttCredentialRepository;
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

    // TODO: Separate DB entry initialization & Broker user initialization?
    sync(mqttCredential);

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
  public void syncAll() {
    try (final var credentials = mqttCredentialRepository.findAllBy()) {
      credentials.forEach(this::sync);
    }
  }

  @Override
  public void sync(MqttCredential mqttCredential) {
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

  private void deleteMqttCredentialAtBroker(String username) throws MqttCredentialServiceException {
    this.emqxApiClient.deleteEmqxAuthUser(username);
    this.emqxApiClient.deleteRulesOfAuthUser(username);
  }
}
