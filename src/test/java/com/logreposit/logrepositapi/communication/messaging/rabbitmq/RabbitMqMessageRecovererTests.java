package com.logreposit.logrepositapi.communication.messaging.rabbitmq;

import com.logreposit.logrepositapi.configuration.ApplicationConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RabbitMqMessageRecovererTests
{
    private static final String MESSAGE_ERROR_COUNT_HEADER_KEY = "x-error-count";
    private static final String CONSUMER_QUEUE = "q.consumer-queue";

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Captor
    private ArgumentCaptor<String> exchangeCaptor;

    @Captor
    private ArgumentCaptor<String> consumerQueueCaptor;

    @Captor
    private ArgumentCaptor<Message> messageCaptor;

    private RabbitMqMessageRecoverer messageRecoverer;

    @BeforeEach
    public void setUp() {
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();

        applicationConfiguration.setMessageRetryIntervals(List.of(10000, 30000, 300000));

        this.messageRecoverer = new RabbitMqMessageRecoverer(this.rabbitTemplate, new RabbitRetryStrategy(applicationConfiguration));
    }

    @Test
    public void testRecover_givenNoErrorCountHeader_expectFirstRetryExchange() {
        var message = givenMessageWithErrorCount(null);

        this.messageRecoverer.recover(message, new Throwable());

        verify(this.rabbitTemplate, times(1)).convertAndSend(
                this.exchangeCaptor.capture(),
                this.consumerQueueCaptor.capture(),
                this.messageCaptor.capture()
        );

        assertThat(this.exchangeCaptor.getValue()).isEqualTo("retry.x.10000");
        assertThat(this.consumerQueueCaptor.getValue()).isEqualTo(CONSUMER_QUEUE);
        assertPersistentAndErrorCountIs(this.messageCaptor.getValue(), 1);
    }

    @Test
    public void testRecover_givenErrorCount0_expectFirstRetryExchange() {
        var message = givenMessageWithErrorCount(0L);

        this.messageRecoverer.recover(message, new Throwable());

        verify(this.rabbitTemplate, times(1)).convertAndSend(
                this.exchangeCaptor.capture(),
                this.consumerQueueCaptor.capture(),
                this.messageCaptor.capture()
        );

        assertThat(this.exchangeCaptor.getValue()).isEqualTo("retry.x.10000");
        assertThat(this.consumerQueueCaptor.getValue()).isEqualTo(CONSUMER_QUEUE);
        assertPersistentAndErrorCountIs(this.messageCaptor.getValue(), 1);
    }

    @Test
    public void testRecover_givenErrorCount1_expectFirstRetryExchange() {
        var message = givenMessageWithErrorCount(1L);

        this.messageRecoverer.recover(message, new Throwable());

        verify(this.rabbitTemplate, times(1)).convertAndSend(
                this.exchangeCaptor.capture(),
                this.consumerQueueCaptor.capture(),
                this.messageCaptor.capture()
        );

        assertThat(this.exchangeCaptor.getValue()).isEqualTo("retry.x.10000");
        assertThat(this.consumerQueueCaptor.getValue()).isEqualTo(CONSUMER_QUEUE);
        assertPersistentAndErrorCountIs(this.messageCaptor.getValue(), 2);
    }

    @Test
    public void testRecover_givenErrorCount9_expect2ndRetryExchange() {
        var message = givenMessageWithErrorCount(9L);

        this.messageRecoverer.recover(message, new Throwable());

        verify(this.rabbitTemplate, times(1)).convertAndSend(
                this.exchangeCaptor.capture(),
                this.consumerQueueCaptor.capture(),
                this.messageCaptor.capture()
        );

        assertThat(this.exchangeCaptor.getValue()).isEqualTo("retry.x.30000");
        assertThat(this.consumerQueueCaptor.getValue()).isEqualTo(CONSUMER_QUEUE);
        assertPersistentAndErrorCountIs(this.messageCaptor.getValue(), 10);
    }

    @Test
    public void testRecover_givenErrorCount14_expect2ndRetryExchange() {
        var message = givenMessageWithErrorCount(14L);

        this.messageRecoverer.recover(message, new Throwable());

        verify(this.rabbitTemplate, times(1)).convertAndSend(
                this.exchangeCaptor.capture(),
                this.consumerQueueCaptor.capture(),
                this.messageCaptor.capture()
        );

        assertThat(this.exchangeCaptor.getValue()).isEqualTo("retry.x.300000");
        assertThat(this.consumerQueueCaptor.getValue()).isEqualTo(CONSUMER_QUEUE);
        assertPersistentAndErrorCountIs(this.messageCaptor.getValue(), 15);
    }

    @Test
    public void testRecover_givenErrorCount15_expectErrorExchange() {
        var message = givenMessageWithErrorCount(15L);

        this.messageRecoverer.recover(message, new Throwable());

        verify(this.rabbitTemplate, times(1)).convertAndSend(
                this.exchangeCaptor.capture(),
                this.consumerQueueCaptor.capture(),
                this.messageCaptor.capture()
        );

        assertThat(this.exchangeCaptor.getValue()).isEqualTo("error.x");
        assertThat(this.consumerQueueCaptor.getValue()).isEqualTo(CONSUMER_QUEUE);
        assertPersistentAndErrorCountIs(this.messageCaptor.getValue(), 16);
    }

    private static Message givenMessageWithErrorCount(Long errorCount) {
        var messageProperties = new MessageProperties();

        messageProperties.getHeaders().put(MESSAGE_ERROR_COUNT_HEADER_KEY, errorCount);
        messageProperties.setConsumerQueue(CONSUMER_QUEUE);

        return new Message(null, messageProperties);
    }

    private static void assertPersistentAndErrorCountIs(Message message, long errorCount) {
        var messageProperties = message.getMessageProperties();

        assertThat(messageProperties.getDeliveryMode()).isEqualTo(MessageDeliveryMode.PERSISTENT);
        assertThat(messageProperties.getHeaders()).hasFieldOrProperty(MESSAGE_ERROR_COUNT_HEADER_KEY);

        Object count = messageProperties.getHeader(MESSAGE_ERROR_COUNT_HEADER_KEY);

        assertThat(count).isNotNull();
        assertThat(count).isInstanceOf(Long.class);
        assertThat((Long) count).isEqualTo(errorCount);
    }
}
