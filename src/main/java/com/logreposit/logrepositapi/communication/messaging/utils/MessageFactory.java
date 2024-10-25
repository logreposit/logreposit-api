package com.logreposit.logrepositapi.communication.messaging.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.common.MessageMetaData;
import com.logreposit.logrepositapi.communication.messaging.common.MessageType;
import com.logreposit.logrepositapi.communication.messaging.dtos.DeviceCreatedMessageDto;
import com.logreposit.logrepositapi.communication.messaging.dtos.UserCreatedMessageDto;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.ReadingDto;
import com.logreposit.logrepositapi.rest.filters.RequestCorrelation;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MessageFactory {
  private final ObjectMapper objectMapper;

  public MessageFactory(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public Message buildEventBMV600LogdataReceivedMessage(
      Object bmv600LogData, String deviceId, String userId) throws JsonProcessingException {
    MessageMetaData messageMetaData = new MessageMetaData();
    messageMetaData.setDeviceId(deviceId);
    messageMetaData.setUserId(userId);

    Message message = createMessage(messageMetaData);

    message.setType(MessageType.EVENT_BMV_600_LOGDATA_RECEIVED.toString());
    message.setPayload(this.objectMapper.writeValueAsString(bmv600LogData));

    addCorrelationIdToMessage(message);

    return message;
  }

  public Message buildEventSolarLogLogdataReceivedMessage(
      Object solarLogLogData, String deviceId, String userId) throws JsonProcessingException {
    MessageMetaData messageMetaData = new MessageMetaData();
    messageMetaData.setDeviceId(deviceId);
    messageMetaData.setUserId(userId);

    Message message = createMessage(messageMetaData);

    message.setType(MessageType.EVENT_SOLARLOG_LOGDATA_RECEIVED.toString());
    message.setPayload(this.objectMapper.writeValueAsString(solarLogLogData));

    addCorrelationIdToMessage(message);

    return message;
  }

  public Message buildEventFroelingS3200LogdataReceivedMessage(
      Object froelingLogData, String deviceId, String userId) throws JsonProcessingException {
    MessageMetaData messageMetaData = new MessageMetaData();
    messageMetaData.setDeviceId(deviceId);
    messageMetaData.setUserId(userId);

    Message message = createMessage(messageMetaData);

    message.setType(MessageType.EVENT_FROELING_LAMBDATRONIC_S3200_LOGDATA_RECEIVED.toString());
    message.setPayload(this.objectMapper.writeValueAsString(froelingLogData));

    addCorrelationIdToMessage(message);

    return message;
  }

  public Message buildEventCotekSPSeriesLogdataReceivedMessage(
      Object cotekLogData, String deviceId, String userId) throws JsonProcessingException {
    MessageMetaData messageMetaData = new MessageMetaData();
    messageMetaData.setDeviceId(deviceId);
    messageMetaData.setUserId(userId);

    Message message = createMessage(messageMetaData);

    message.setType(MessageType.EVENT_COTEK_SP_SERIES_LOGDATA_RECEIVED.toString());
    message.setPayload(this.objectMapper.writeValueAsString(cotekLogData));

    addCorrelationIdToMessage(message);

    return message;
  }

  public Message buildEventCCS811LogdataReceivedMessage(
      Object ccs811LogData, String deviceId, String userId) throws JsonProcessingException {
    MessageMetaData messageMetaData = new MessageMetaData();
    messageMetaData.setDeviceId(deviceId);
    messageMetaData.setUserId(userId);

    Message message = createMessage(messageMetaData);

    message.setType(MessageType.EVENT_CCS811_LOGDATA_RECEIVED.toString());
    message.setPayload(this.objectMapper.writeValueAsString(ccs811LogData));

    addCorrelationIdToMessage(message);

    return message;
  }

  public Message buildEventDHTLogdataReceivedMessage(
      Object dhtLogData, String deviceId, String userId) throws JsonProcessingException {
    MessageMetaData messageMetaData = new MessageMetaData();
    messageMetaData.setDeviceId(deviceId);
    messageMetaData.setUserId(userId);

    Message message = createMessage(messageMetaData);

    message.setType(MessageType.EVENT_DHT_LOGDATA_RECEIVED.toString());
    message.setPayload(this.objectMapper.writeValueAsString(dhtLogData));

    addCorrelationIdToMessage(message);

    return message;
  }

  public Message buildEventGenericLogdataReceivedMessage(
      List<ReadingDto> readings, String deviceId, String userId) throws JsonProcessingException {
    MessageMetaData messageMetaData = new MessageMetaData();
    messageMetaData.setDeviceId(deviceId);
    messageMetaData.setUserId(userId);

    Message message = createMessage(messageMetaData);

    message.setType(MessageType.EVENT_GENERIC_LOGDATA_RECEIVED.toString());
    message.setPayload(this.objectMapper.writeValueAsString(readings));

    addCorrelationIdToMessage(message);

    return message;
  }

  public Message buildEventUserCreatedMessage(UserCreatedMessageDto user)
      throws JsonProcessingException {
    MessageMetaData messageMetaData = new MessageMetaData();
    Message message = createMessage(messageMetaData);

    message.setType(MessageType.EVENT_USER_CREATED.toString());
    message.setPayload(this.objectMapper.writeValueAsString(user));

    addCorrelationIdToMessage(message);

    return message;
  }

  public Message buildEventDeviceCreatedMessage(
      DeviceCreatedMessageDto device, String userId, String userEmail)
      throws JsonProcessingException {
    MessageMetaData messageMetaData = new MessageMetaData();

    messageMetaData.setUserId(userId);
    messageMetaData.setUserEmail(userEmail);
    messageMetaData.setDeviceId(device.getId());

    Message message = createMessage(messageMetaData);

    message.setType(MessageType.EVENT_DEVICE_CREATED.toString());
    message.setPayload(this.objectMapper.writeValueAsString(device));

    addCorrelationIdToMessage(message);

    return message;
  }

  private static void addCorrelationIdToMessage(Message message) {
    String correlationId = RequestCorrelation.getCorrelationId();

    message.getMetaData().setCorrelationId(correlationId);
  }

  private static Message createMessage(MessageMetaData messageMetaData) {
    Message message = new Message();

    message.setDate(new Date());
    message.setId(UUID.randomUUID().toString());
    message.setMetaData(messageMetaData);

    return message;
  }
}
