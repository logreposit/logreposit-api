package com.logreposit.logrepositapi.services.mqtt;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logreposit.logrepositapi.configuration.MqttConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MqttClientProviderTests {
  @Mock private MqttConfiguration mqttConfiguration;

  private MqttClientProvider mqttClientProvider;

  @BeforeEach
  public void setUp() {
    this.mqttClientProvider = new MqttClientProvider(mqttConfiguration);
  }

  @Test
  public void testGetMqttClient_givenMqttSupportDisabled_expectThrowsException() {
    when(mqttConfiguration.isEnabled()).thenReturn(false);

    assertThatThrownBy(() -> mqttClientProvider.getMqttClient("myUser", "myPassword"))
        .isExactlyInstanceOf(MqttClientProviderException.class)
        .hasMessage("MQTT support is not enabled!");

    verify(mqttConfiguration).isEnabled();
  }

  // TODO DoM: think about how to test the actual getMqttClient method, as it actually creates an
  // TODO DoM: MqttClient instance and connects to the broker.
}
