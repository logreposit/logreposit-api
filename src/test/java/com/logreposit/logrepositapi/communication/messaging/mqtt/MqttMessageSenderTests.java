package com.logreposit.logrepositapi.communication.messaging.mqtt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.configuration.MqttConfiguration;
import com.logreposit.logrepositapi.persistence.documents.MqttCredential;
import com.logreposit.logrepositapi.services.mqtt.MqttClientProvider;
import com.logreposit.logrepositapi.services.mqtt.MqttCredentialService;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MqttMessageSenderTests {
  private static final String SAMPLE_TOPIC = "myTopic";
  private static final Map<String, String> SAMPLE_MESSAGE = sampleMessage();

  private static final MqttCredential SAMPLE_MQTT_CREDENTIAL = sampleMqttCredential();

  private MqttMessageSender mqttMessageSender;

  @Mock private MqttConfiguration mqttConfiguration;
  @Mock private MqttClientProvider mqttClientProvider;
  @Mock private MqttCredentialService mqttCredentialService;
  @Mock private IMqttClient mqttClient;

  @BeforeEach
  public void setUp() {
    final var objectMapper = new ObjectMapper();

    mqttMessageSender =
        new MqttMessageSender(
            objectMapper, mqttConfiguration, mqttClientProvider, mqttCredentialService);
  }

  @Test
  public void testSend_givenMqttDisabledInConfig_expectDoesNotSendAnything() throws MqttException {
    when(mqttConfiguration.isEnabled()).thenReturn(false);

    mqttMessageSender.send(SAMPLE_TOPIC, SAMPLE_MESSAGE);

    verify(mqttCredentialService, never()).getGlobalDeviceDataWriteCredential();
    verify(mqttClientProvider, never()).getMqttClient(any(), any());
  }

  @Test
  public void testSend_givenMqttIsEnabled_expectMessageIsBeingSent() throws MqttException {
    when(mqttConfiguration.isEnabled()).thenReturn(true);
    when(mqttCredentialService.getGlobalDeviceDataWriteCredential())
        .thenReturn(SAMPLE_MQTT_CREDENTIAL);
    when(mqttClientProvider.getMqttClient(
            eq(SAMPLE_MQTT_CREDENTIAL.getUsername()), eq(sampleMqttCredential().getPassword())))
        .thenReturn(mqttClient);

    mqttMessageSender.send(SAMPLE_TOPIC, SAMPLE_MESSAGE);

    final var topicArgumentCaptor = ArgumentCaptor.forClass(String.class);
    final var mqttMessageArgumentCaptor = ArgumentCaptor.forClass(MqttMessage.class);

    verify(mqttCredentialService).getGlobalDeviceDataWriteCredential();
    verify(mqttClientProvider)
        .getMqttClient(
            eq(SAMPLE_MQTT_CREDENTIAL.getUsername()), eq(sampleMqttCredential().getPassword()));
    verify(mqttClient).publish(topicArgumentCaptor.capture(), mqttMessageArgumentCaptor.capture());

    assertThat(topicArgumentCaptor.getValue()).isNotNull();
    assertThat(mqttMessageArgumentCaptor.getValue()).isNotNull();

    assertThat(topicArgumentCaptor.getValue()).isEqualTo(SAMPLE_TOPIC);

    final var capturedMqttMessage = mqttMessageArgumentCaptor.getValue();
    final var capturedPayload =
        new String(capturedMqttMessage.getPayload(), StandardCharsets.UTF_8);

    assertThat(capturedPayload).isEqualTo("{\"myName\":\"myEvent\",\"myPayload\":\"myText\"}");
  }

  @Test
  public void testSend_twice_givenMqttIsEnabled_expectClientIsBeingCached() throws MqttException {
    when(mqttConfiguration.isEnabled()).thenReturn(true);
    when(mqttCredentialService.getGlobalDeviceDataWriteCredential())
        .thenReturn(SAMPLE_MQTT_CREDENTIAL);
    when(mqttClientProvider.getMqttClient(
            eq(SAMPLE_MQTT_CREDENTIAL.getUsername()), eq(sampleMqttCredential().getPassword())))
        .thenReturn(mqttClient);

    mqttMessageSender.send(SAMPLE_TOPIC, SAMPLE_MESSAGE);
    mqttMessageSender.send("mySecondTopic", SAMPLE_MESSAGE);

    final var topicArgumentCaptor = ArgumentCaptor.forClass(String.class);
    final var mqttMessageArgumentCaptor = ArgumentCaptor.forClass(MqttMessage.class);

    verify(mqttCredentialService).getGlobalDeviceDataWriteCredential();
    verify(mqttClientProvider)
        .getMqttClient(
            eq(SAMPLE_MQTT_CREDENTIAL.getUsername()), eq(sampleMqttCredential().getPassword()));
    verify(mqttClient, times(2))
        .publish(topicArgumentCaptor.capture(), mqttMessageArgumentCaptor.capture());

    assertThat(topicArgumentCaptor.getAllValues()).hasSize(2);
    assertThat(mqttMessageArgumentCaptor.getAllValues()).hasSize(2);

    assertThat(topicArgumentCaptor.getAllValues().get(0)).isEqualTo(SAMPLE_TOPIC);
    assertThat(topicArgumentCaptor.getAllValues().get(1)).isEqualTo("mySecondTopic");
  }

  @Test
  public void testSend_givenMqttClientProviderReturnsNull_expectIllegalStateException()
      throws MqttException {
    when(mqttConfiguration.isEnabled()).thenReturn(true);
    when(mqttCredentialService.getGlobalDeviceDataWriteCredential())
        .thenReturn(SAMPLE_MQTT_CREDENTIAL);
    when(mqttClientProvider.getMqttClient(
            eq(SAMPLE_MQTT_CREDENTIAL.getUsername()), eq(sampleMqttCredential().getPassword())))
        .thenReturn(null);

    assertThatThrownBy(() -> mqttMessageSender.send(SAMPLE_TOPIC, SAMPLE_MESSAGE))
        .isExactlyInstanceOf(IllegalStateException.class)
        .hasMessage("mqttClient should have been initialized before!");

    verify(mqttCredentialService).getGlobalDeviceDataWriteCredential();
    verify(mqttClientProvider)
        .getMqttClient(
            eq(SAMPLE_MQTT_CREDENTIAL.getUsername()), eq(sampleMqttCredential().getPassword()));
  }

  @Test
  public void testSend_givenMqttClientThrowsExceptionOnPublish_expectMqttMessageSenderException()
      throws MqttException {
    when(mqttConfiguration.isEnabled()).thenReturn(true);
    when(mqttCredentialService.getGlobalDeviceDataWriteCredential())
        .thenReturn(SAMPLE_MQTT_CREDENTIAL);
    when(mqttClientProvider.getMqttClient(
            eq(SAMPLE_MQTT_CREDENTIAL.getUsername()), eq(sampleMqttCredential().getPassword())))
        .thenReturn(mqttClient);

    doThrow(new MqttException(0)).when(mqttClient).publish(eq(SAMPLE_TOPIC), any());

    assertThatThrownBy(() -> mqttMessageSender.send(SAMPLE_TOPIC, SAMPLE_MESSAGE))
        .isExactlyInstanceOf(MqttMessageSenderException.class)
        .hasMessage("Unable to publish MQTT message");

    verify(mqttCredentialService).getGlobalDeviceDataWriteCredential();
    verify(mqttClientProvider)
        .getMqttClient(
            eq(SAMPLE_MQTT_CREDENTIAL.getUsername()), eq(sampleMqttCredential().getPassword()));
    verify(mqttClient).publish(eq(SAMPLE_TOPIC), any());
  }

  private static MqttCredential sampleMqttCredential() {
    final var mqttCredential = new MqttCredential();

    mqttCredential.setUsername("myUser");
    mqttCredential.setPassword("myPassword");

    return mqttCredential;
  }

  private static Map<String, String> sampleMessage() {
    final var message = new LinkedHashMap<String, String>();

    message.put("myName", "myEvent");
    message.put("myPayload", "myText");

    return message;
  }
}
