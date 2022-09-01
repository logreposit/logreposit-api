package com.logreposit.logrepositapi.services.mqtt;

import com.logreposit.logrepositapi.persistence.documents.MqttCredential;
import org.springframework.data.domain.Page;

public interface MqttService {
  // TODO DoM: in the future, allow also specifying role names. Currently only device read only is
  // supported.
  MqttCredential create(String userId, String description) throws MqttServiceException;

  Page<MqttCredential> list(String userId, Integer page, Integer size);

  MqttCredential get(String mqttCredentialId, String userId) throws MqttCredentialNotFoundException;

  MqttCredential delete(String mqttCredentialId, String userId) throws MqttServiceException;
}
