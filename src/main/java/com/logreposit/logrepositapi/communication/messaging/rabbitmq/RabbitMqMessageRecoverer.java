package com.logreposit.logrepositapi.communication.messaging.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;

public class RabbitMqMessageRecoverer implements MessageRecoverer
{
    private static final Logger logger = LoggerFactory.getLogger(RabbitMqMessageRecoverer.class);

    private static final String MESSAGE_ERROR_COUNT_HEADER_KEY = "x-error-count";

    private final RabbitTemplate rabbitTemplate;

    public RabbitMqMessageRecoverer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void recover(Message message, Throwable throwable)
    {
        logger.error("Cannot process AMQP message", throwable);

        long errorCount = getMessageErrorCount(message);

        message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        message.getMessageProperties().setHeader(MESSAGE_ERROR_COUNT_HEADER_KEY, errorCount + 1);

        String exchange = getExchangeName(errorCount);
        String consumerQueue = message.getMessageProperties().getConsumerQueue();

        this.rabbitTemplate.convertAndSend(
                exchange,
                consumerQueue,
                message
        );

        logger.info("Republished message after {} error(s) to '{}' with routing key '{}'", errorCount, exchange, consumerQueue);
    }

    private static long getMessageErrorCount(Message amqpMessage)
    {
        Object errorCountAsObject = amqpMessage.getMessageProperties()
                                               .getHeaders()
                                               .get(MESSAGE_ERROR_COUNT_HEADER_KEY);

        if (errorCountAsObject == null) {
            return 0;
        }

        if (!(errorCountAsObject instanceof Number)) {
            return 0;
        }

        long errorCount = ((Number) errorCountAsObject).longValue();

        logger.info("Retrieved message error count: {}", errorCount);

        return errorCount;
    }

    private static String getExchangeName(long errorCount) {
        if (errorCount <= 5)
            return "retry.x.10000";

        if (errorCount <= 10)
            return "retry.x.30000";

        if (errorCount <= 15)
            return "retry.x.300000";

        return "error.x";
    }
}
