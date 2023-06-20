package com.logreposit.logrepositapi.services.mqtt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logreposit.logrepositapi.configuration.MqttConfiguration;
import java.net.UnknownHostException;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MqttClientProviderTests {
  @Mock private MqttConfiguration mqttConfiguration;

  @Test
  public void testGetMqttClient_givenMqttSupportDisabled_expectThrowsException() {
    final var mqttClientProvider = new MqttClientProvider(mqttConfiguration, new MqttClientCache());

    when(mqttConfiguration.isEnabled()).thenReturn(false);

    assertThatThrownBy(() -> mqttClientProvider.getMqttClient("myUser", "myPassword"))
        .isExactlyInstanceOf(MqttClientProviderException.class)
        .hasMessage("MQTT support is not enabled!");

    verify(mqttConfiguration).isEnabled();
  }

  @Test
  public void
      testGetMqttClient_givenMqttSupportEnabled_expectTriesToConnectToConfiguredHostAndThrowsException() {
    final var mqttClientProvider = new MqttClientProvider(mqttConfiguration, new MqttClientCache());

    when(mqttConfiguration.isEnabled()).thenReturn(true);
    when(mqttConfiguration.getHost()).thenReturn("unknownMqttHost");

    assertThatThrownBy(() -> mqttClientProvider.getMqttClient("myUser", "myPassword"))
        .isExactlyInstanceOf(MqttException.class)
        .hasMessage("MqttException")
        .hasCauseInstanceOf(UnknownHostException.class)
        .hasRootCauseMessage("unknownMqttHost");

    verify(mqttConfiguration).isEnabled();
  }

  @Test
  public void
      testGetMqttClient_givenMqttSupportEnabledAndClientAlreadyCached_expectReturnsCachedClient()
          throws MqttException {
    final var mqttClientCache = new MqttClientCache();
    final var mqttClientProvider = new MqttClientProvider(mqttConfiguration, mqttClientCache);

    final var alreadyInitializedMqttClient =
        new MqttClient("tcp://host:12345", "myClientId", new MemoryPersistence());

    mqttClientCache.put("myUser", alreadyInitializedMqttClient);

    when(mqttConfiguration.isEnabled()).thenReturn(true);

    final var mqttClient = mqttClientProvider.getMqttClient("myUser", "someRandomPassword");

    assertThat(mqttClient).isSameAs(alreadyInitializedMqttClient);
  }
}
