package com.logreposit.logrepositapi.stuff.mqtt;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.logreposit.logrepositapi.configuration.MqttConfiguration;
import com.logreposit.logrepositapi.services.mqtt.MqttClientProvider;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@Disabled("meant for manual testing")
@ExtendWith(MockitoExtension.class)
public class MqttTests {
  @Test
  public void testCreateClientWithClient()
      throws MqttException,
          ExecutionException,
          JsonProcessingException,
          InterruptedException,
          TimeoutException {
    String clientId = UUID.randomUUID().toString();

    final var options = new MqttConnectOptions();

    options.setUserName("administrator");
    options.setPassword("f2596f3b-0ccf-48d6-9d8b-dba52760c0a0".toCharArray());
    options.setConnectionTimeout(10);
    options.setCleanSession(true);

    try {
      final var mqttClient =
          new MqttClient("tcp://127.0.0.1:1883", "myClientId-" + clientId, new MemoryPersistence());

      if (!mqttClient.isConnected()) {
        System.out.println("We're NOT connected!");
        mqttClient.connect(options);
        System.out.println("We're connected! :D");
      }

      testCreateClientWithClient_givenIMqttClient(mqttClient);

      mqttClient.disconnect();
    } catch (Exception e) {
      System.out.println(
          "We've caught an exception! "
              + e.getClass().getName()
              + " ==> Message is: "
              + e.getMessage());

      throw e;
    }
  }

  private void testCreateClientWithClient_givenIMqttClient(IMqttClient client)
      throws MqttException {
    final var counter = 709;
    final var userName = "myUser" + counter;
    final var roleName = "myRole" + counter;
    final var topic = String.format("logreposit/users/%s/devices/#", userName);

    final var mqttConfigurationMock = Mockito.mock(MqttConfiguration.class);

    when(mqttConfigurationMock.isEnabled()).thenReturn(true);
    when(mqttConfigurationMock.getUsername()).thenReturn("myAdminUser");
    when(mqttConfigurationMock.getPassword()).thenReturn("myAdminPassword");

    final var providerMock = Mockito.mock(MqttClientProvider.class);

    when(providerMock.getMqttClient(eq("myAdminUser"), eq("myAdminPassword"))).thenReturn(client);
  }
}
