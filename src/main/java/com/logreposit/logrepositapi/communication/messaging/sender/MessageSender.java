package com.logreposit.logrepositapi.communication.messaging.sender;

import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessageSenderException;

public interface MessageSender
{
    void send(Message message) throws MessageSenderException;
}
