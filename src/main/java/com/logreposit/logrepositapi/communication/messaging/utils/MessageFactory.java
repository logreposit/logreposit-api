package com.logreposit.logrepositapi.communication.messaging.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.logreposit.logrepositapi.communication.messaging.common.Message;

public interface MessageFactory
{
    Message buildEventCmiLogdataReceivedMessage(Object cmiLogData, String deviceId, String userId) throws JsonProcessingException;
}
