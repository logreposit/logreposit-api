package com.logreposit.logrepositapi.communication.messaging.rabbitmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.common.MessageMetaData;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessageSenderException;
import com.logreposit.logrepositapi.communication.messaging.rabbitmq.RabbitMessageSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ObjectMapper.class})
public class RabbitMessageSenderTests
{
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    private RabbitMessageSender rabbitMessageSender;

    @BeforeEach
    public void setUp()
    {
        this.rabbitMessageSender = new RabbitMessageSender(this.objectMapper, this.rabbitTemplate);
    }

    @Test
    public void testSend_simple() throws MessageSenderException, JsonProcessingException
    {
        Date    now               = new Date();
        Message message           = sampleMessage(now);

        this.rabbitMessageSender.send(message);

        var messageArgumentCaptor = ArgumentCaptor.forClass(org.springframework.amqp.core.Message.class);

        Mockito.verify(this.rabbitTemplate, Mockito.times(1))
               .convertAndSend(Mockito.eq(String.format("x.%s", message.getType())), Mockito.anyString(), messageArgumentCaptor.capture());

        var capturedMessage = messageArgumentCaptor.getValue();

        assertThat(capturedMessage).isNotNull();
        assertThat(capturedMessage.getMessageProperties().getContentType()).isEqualTo("application/json");

        String serializedMessage = this.objectMapper.writeValueAsString(message);

        assertThat(new String(capturedMessage.getBody(), StandardCharsets.UTF_8)).isEqualTo(serializedMessage);
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
