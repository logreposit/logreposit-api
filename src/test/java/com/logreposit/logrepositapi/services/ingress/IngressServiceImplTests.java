package com.logreposit.logrepositapi.services.ingress;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.common.MessageMetaData;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessageSenderException;
import com.logreposit.logrepositapi.communication.messaging.sender.MessageSender;
import com.logreposit.logrepositapi.communication.messaging.utils.MessageFactory;
import com.logreposit.logrepositapi.configuration.ApplicationConfiguration;
import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.rest.dtos.DeviceType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RunWith(SpringRunner.class)
public class IngressServiceImplTests
{
    private static final int    MESSAGE_SENDER_RETRY_COUNT              = 3;
    private static final long   MESSAGE_SENDER_INITIAL_BACKOFF_INTERVAL = 10;
    private static final double MESSAGE_SENDER_BACKOFF_MULTIPLIER       = 1.1;

    @MockBean
    private ApplicationConfiguration applicationConfiguration;

    @MockBean
    private MessageSender messageSender;

    @MockBean
    private MessageFactory messageFactory;

    private IngressServiceImpl ingressService;

    @Before
    public void setUp()
    {
        this.ingressService = new IngressServiceImpl(this.applicationConfiguration, this.messageSender, this.messageFactory);

        Mockito.when(this.applicationConfiguration.getMessageSenderRetryCount()).thenReturn(MESSAGE_SENDER_RETRY_COUNT);
        Mockito.when(this.applicationConfiguration.getMessageSenderRetryInitialBackOffInterval()).thenReturn(MESSAGE_SENDER_INITIAL_BACKOFF_INTERVAL);
        Mockito.when(this.applicationConfiguration.getMessageSenderBackOffMultiplier()).thenReturn(MESSAGE_SENDER_BACKOFF_MULTIPLIER);
    }

    @Test
    public void testProcessData() throws JsonProcessingException, IngressServiceException, MessageSenderException
    {
        Device     device     = getTestDevice();
        DeviceType deviceType = DeviceType.TECHNISCHE_ALTERNATIVE_CMI;
        Object     data       = getTestData();
        Message    message    = getTestMessage();

        Mockito.when(this.messageFactory.buildEventCmiLogdataReceivedMessage(Mockito.any(Object.class), Mockito.eq(device.getId()), Mockito.eq(device.getUserId())))
               .thenReturn(message);

        this.ingressService.processData(device, deviceType, data);

        ArgumentCaptor<Object> dataArgumentCaptor = ArgumentCaptor.forClass(Object.class);

        Mockito.verify(
                this.messageFactory,
                Mockito.times(1)).buildEventCmiLogdataReceivedMessage(dataArgumentCaptor.capture(), Mockito.eq(device.getId()), Mockito.eq(device.getUserId())
        );

        Mockito.verify(this.messageSender, Mockito.times(1)).send(Mockito.same(message));
    }

    @Test(expected = UnsupportedDeviceTypeException.class)
    public void testProcessData_unknownDeviceType() throws IngressServiceException
    {
        Device     device     = getTestDevice();
        DeviceType deviceType = DeviceType.UNKNOWN;
        Object     data       = getTestData();

        this.ingressService.processData(device, deviceType, data);
    }

    @Test
    public void testProcessData_throwsJsonProcessingException() throws JsonProcessingException
    {
        Device     device     = getTestDevice();
        DeviceType deviceType = DeviceType.TECHNISCHE_ALTERNATIVE_CMI;
        Object     data       = getTestData();

        Mockito.when(this.messageFactory.buildEventCmiLogdataReceivedMessage(Mockito.any(Object.class), Mockito.eq(device.getId()), Mockito.eq(device.getUserId())))
               .thenThrow(new TestJsonProcessingException(""));

        try
        {
            this.ingressService.processData(device, deviceType, data);

            Assert.fail("Should not be here.");
        }
        catch (IngressServiceException e)
        {
            Assert.assertEquals("Unable to create Log Data Received Message", e.getMessage());
        }
    }

    private static Device getTestDevice()
    {
        Device device = new Device();

        device.setId(UUID.randomUUID().toString());
        device.setUserId(UUID.randomUUID().toString());
        device.setName(UUID.randomUUID().toString());

        return device;
    }

    private static Object getTestData()
    {
        Map<String, Object> dataMap = new HashMap<>();

        dataMap.put("date", new Date());

        return dataMap;
    }

    private static Message getTestMessage()
    {
        Message message = new Message();

        message.setId(UUID.randomUUID().toString());
        message.setDate(new Date());
        message.setType("type");
        message.setMetaData(new MessageMetaData());
        message.setPayload("");

        return message;
    }
}
