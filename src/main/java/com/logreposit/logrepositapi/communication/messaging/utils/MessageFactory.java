package com.logreposit.logrepositapi.communication.messaging.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.dtos.DeviceCreatedMessageDto;
import com.logreposit.logrepositapi.communication.messaging.dtos.UserCreatedMessageDto;

public interface MessageFactory
{
    Message buildEventCmiLogdataReceivedMessage        (Object cmiLogData, String deviceId, String userId)               throws JsonProcessingException;
    Message buildEventBMV600LogdataReceivedMessage     (Object bmv600LogData, String deviceId, String userId)            throws JsonProcessingException;
    Message buildEventLacrosseTXLogdataReceivedMessage (Object lacrosseTxLogData, String deviceId, String userId)        throws JsonProcessingException;
    Message buildEventUserCreatedMessage               (UserCreatedMessageDto user)                                      throws JsonProcessingException;
    Message buildEventDeviceCreatedMessage             (DeviceCreatedMessageDto device, String userId, String userEmail) throws JsonProcessingException;
}
