package com.logreposit.logrepositapi.communication.messaging.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.dtos.UserCreatedMessageDto;

public interface MessageFactory
{
    Message buildEventCmiLogdataReceivedMessage (Object cmiLogData, String deviceId, String userId) throws JsonProcessingException;
    Message buildUserCreatedMessage             (UserCreatedMessageDto user)                        throws JsonProcessingException;
}
