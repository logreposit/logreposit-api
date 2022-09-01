package com.logreposit.logrepositapi.stuff.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.communication.messaging.mqtt.MosquittoDynSecClient;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.AddClientRoleCommand;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.AddRoleAclCommand;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.CreateClientCommand;
import com.logreposit.logrepositapi.communication.messaging.mqtt.control.CreateRoleCommand;
import java.util.List;
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

@Disabled("meant for manual testing")
public class MqttTests {
  @Test
  public void testCreateClientWithClient()
      throws MqttException, ExecutionException, JsonProcessingException, InterruptedException,
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
      throws MqttException, ExecutionException, JsonProcessingException, InterruptedException,
          TimeoutException {
    final var counter = 709;
    final var userName = "myUser" + counter;
    final var roleName = "myRole" + counter;
    final var topic = String.format("logreposit/users/%s/devices/#", userName);

    final var mosquittoDynSecClient = new MosquittoDynSecClient(new ObjectMapper(), client);

    final var commands =
        List.of(
            new CreateRoleCommand(roleName),
            new AddRoleAclCommand(roleName, "subscribePattern", topic, true),
            new CreateClientCommand(userName, userName + "-password0"),
            new AddClientRoleCommand(userName, roleName));

    final var allResponses = mosquittoDynSecClient.sendCommands(commands);
  }
}
