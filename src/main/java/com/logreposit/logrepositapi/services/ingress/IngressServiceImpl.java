package com.logreposit.logrepositapi.services.ingress;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.logreposit.logrepositapi.communication.messaging.common.Message;
import com.logreposit.logrepositapi.communication.messaging.exceptions.MessageSenderException;
import com.logreposit.logrepositapi.communication.messaging.rabbitmq.RabbitMessageSender;
import com.logreposit.logrepositapi.communication.messaging.utils.MessageFactory;
import com.logreposit.logrepositapi.configuration.ApplicationConfiguration;
import com.logreposit.logrepositapi.persistence.documents.Device;
import com.logreposit.logrepositapi.rest.dtos.DeviceType;
import com.logreposit.logrepositapi.rest.dtos.request.ingress.ReadingDto;
import com.logreposit.logrepositapi.utils.LoggingUtils;
import com.logreposit.logrepositapi.utils.RetryTemplateFactory;
import com.logreposit.logrepositapi.utils.definition.DefinitionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngressServiceImpl implements IngressService
{
    private static final Logger logger = LoggerFactory.getLogger(IngressServiceImpl.class);

    private final ApplicationConfiguration applicationConfiguration;
    private final RabbitMessageSender      messageSender;
    private final MessageFactory           messageFactory;

    public IngressServiceImpl(ApplicationConfiguration applicationConfiguration, RabbitMessageSender messageSender, MessageFactory messageFactory)
    {
        this.applicationConfiguration = applicationConfiguration;
        this.messageSender            = messageSender;
        this.messageFactory           = messageFactory;
    }

    @Override
    public void processData(Device device, DeviceType deviceType, Object data) throws IngressServiceException
    {
        Message message = this.buildMessage(device, deviceType, data);

        this.sendMessage(message);
    }

    @Override
    public void processData(Device device, List<ReadingDto> readings) throws IngressServiceException
    {
        DefinitionValidator.forDefinition(device.getDefinition()).validate(readings);

        Message message = this.buildMessage(device, readings);

        this.sendMessage(message);
    }

    private Message buildMessage(Device device, DeviceType deviceType, Object data) throws IngressServiceException
    {
        try
        {
            switch (deviceType)
            {
                case TECHNISCHE_ALTERNATIVE_CMI:
                    return this.buildTechnischeAlternativeCmiLogDataMessage(device, data);
                case VICTRON_ENERGY_BMV600:
                    return this.buildVictronEnergyBMV600LogDataMessage(device, data);
                case LACROSSE_TECHNOLOGY_TX:
                    return this.buildLacrosseTXLogDataMessage(device, data);
                case SDS_SOLARLOG:
                    return this.buildSolarLogLogDataMessage(device, data);
                case FROELING_LAMBDATRONIC_S3200:
                    return this.buildFroelingLambdatronicS3200LogDataMessage(device, data);
                case COTEK_SP_SERIES:
                    return this.buildCotekSPSeriesLogDataMessage(device, data);
                case CCS811:
                    return this.buildCCS811LogDataMessage(device, data);
                case DHT:
                    return this.buildDHTLogDataMessage(device, data);
                default:
                    throw new UnsupportedDeviceTypeException(deviceType);
            }
        }
        catch (JsonProcessingException e)
        {
            logger.error("Unable to create Log Data Received Message: {}", LoggingUtils.getLogForException(e));
            throw new IngressServiceException("Unable to create Log Data Received Message", e);
        }
    }

    private Message buildMessage(Device device, List<ReadingDto> readings) throws IngressServiceException
    {
        try
        {
            return this.messageFactory.buildEventGenericLogdataReceivedMessage(readings, device.getId(), device.getUserId());
        }
        catch (JsonProcessingException e)
        {
            logger.error("Unable to create Log Data Received Message: {}", LoggingUtils.getLogForException(e));
            throw new IngressServiceException("Unable to create Log Data Received Message", e);
        }
    }

    private Message buildTechnischeAlternativeCmiLogDataMessage(Device device, Object data) throws JsonProcessingException
    {
        Message message = this.messageFactory.buildEventCmiLogdataReceivedMessage(data, device.getId(), device.getUserId());

        return message;
    }

    private Message buildVictronEnergyBMV600LogDataMessage(Device device, Object data) throws JsonProcessingException
    {
        Message message = this.messageFactory.buildEventBMV600LogdataReceivedMessage(data, device.getId(), device.getUserId());

        return message;
    }

    private Message buildLacrosseTXLogDataMessage(Device device, Object data) throws JsonProcessingException
    {
        Message message = this.messageFactory.buildEventLacrosseTXLogdataReceivedMessage(data, device.getId(), device.getUserId());

        return message;
    }

    private Message buildSolarLogLogDataMessage(Device device, Object data) throws JsonProcessingException
    {
        Message message = this.messageFactory.buildEventSolarLogLogdataReceivedMessage(data, device.getId(), device.getUserId());

        return message;
    }

    private Message buildFroelingLambdatronicS3200LogDataMessage(Device device, Object data) throws JsonProcessingException
    {
        Message message = this.messageFactory.buildEventFroelingS3200LogdataReceivedMessage(data, device.getId(), device.getUserId());

        return message;
    }

    private Message buildCotekSPSeriesLogDataMessage(Device device, Object data) throws JsonProcessingException
    {
        Message message = this.messageFactory.buildEventCotekSPSeriesLogdataReceivedMessage(data, device.getId(), device.getUserId());

        return message;
    }

    private Message buildCCS811LogDataMessage(Device device, Object data) throws JsonProcessingException
    {
        Message message = this.messageFactory.buildEventCCS811LogdataReceivedMessage(data, device.getId(), device.getUserId());

        return message;
    }

    private Message buildDHTLogDataMessage(Device device, Object data) throws JsonProcessingException
    {
        Message message = this.messageFactory.buildEventDHTLogdataReceivedMessage(data, device.getId(), device.getUserId());

        return message;
    }

    private void sendMessage(Message message) throws IngressServiceException
    {
        Integer maxAttempts = this.applicationConfiguration.getMessageSenderRetryCount();

        RetryTemplate retryTemplate = RetryTemplateFactory.createWithExponentialBackOffForAllExceptions(
                maxAttempts,
                this.applicationConfiguration.getMessageSenderRetryInitialBackOffInterval(),
                this.applicationConfiguration.getMessageSenderBackOffMultiplier()
        );

        try
        {
            retryTemplate.execute(retryContext -> {
                logger.info("Retry {}/{}: Sending message {}", retryContext.getRetryCount(), maxAttempts, message.getType());

                this.messageSender.send(message);

                return null;
            });
        }
        catch (MessageSenderException e)
        {
            logger.error("Could not send Message of type {}: {}", message.getType(), LoggingUtils.getLogForException(e));
            throw new IngressServiceException("Could not send Message", e);
        }
    }
}
