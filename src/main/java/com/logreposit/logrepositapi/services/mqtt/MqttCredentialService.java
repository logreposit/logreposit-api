package com.logreposit.logrepositapi.services.mqtt;

import com.logreposit.logrepositapi.persistence.documents.MqttCredential;
import com.logreposit.logrepositapi.persistence.documents.MqttRole;
import java.util.List;
import org.springframework.data.domain.Page;

public interface MqttCredentialService {
  MqttCredential create(String userId, String description, List<MqttRole> roles)
      throws MqttCredentialServiceException;

  Page<MqttCredential> list(String userId, Integer page, Integer size);

  MqttCredential get(String mqttCredentialId, String userId) throws MqttCredentialNotFoundException;

  MqttCredential delete(String mqttCredentialId, String userId)
      throws MqttCredentialServiceException;

  MqttCredential getGlobalDeviceDataWriteCredential();
}
