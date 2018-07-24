package com.logreposit.logrepositapi.communication.messaging.rabbitmq.sender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.common.MessageMetaData;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessageSenderException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ObjectMapper.class})
public class RabbitMessageSenderImplTests
{
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    private RabbitMessageSenderImpl rabbitMessageSender;

    @Before
    public void setUp()
    {
        this.rabbitMessageSender = new RabbitMessageSenderImpl(this.objectMapper, this.rabbitTemplate);
    }

    @Test
    public void testSend_simple() throws MessageSenderException, JsonProcessingException
    {
        Date    now               = new Date();
        Message message           = sampleMessage(now);
        String  serializedMesasge = this.objectMapper.writeValueAsString(message);

        this.rabbitMessageSender.send(message);

        Mockito.verify(this.rabbitTemplate, Mockito.times(1))
               .convertAndSend(Mockito.eq(String.format("x.%s", message.getType())), Mockito.anyString(), Mockito.eq(serializedMesasge));
    }

    private static Message sampleMessage(Date date)
    {
        Message message = new Message();

        message.setId(UUID.randomUUID().toString());
        message.setDate(date);
        message.setType(UUID.randomUUID().toString());
        message.setMetaData(new MessageMetaData());
        message.setPayload(UUID.randomUUID().toString());

        return message;
    }
}
