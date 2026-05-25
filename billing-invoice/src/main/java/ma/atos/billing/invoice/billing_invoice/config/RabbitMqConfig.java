package ma.atos.billing.invoice.billing_invoice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public DirectExchange billingExchange(@Value("${billing.rabbitmq.exchange}") String exchange) {
        return new DirectExchange(exchange);
    }

    @Bean
    public Queue paymentRequestedQueue(@Value("${billing.rabbitmq.payment-requested-queue}") String queue) {
        return new Queue(queue, true);
    }

    @Bean
    public Queue paymentCompletedQueue(@Value("${billing.rabbitmq.payment-completed-queue}") String queue) {
        return new Queue(queue, true);
    }

    @Bean
    public Binding paymentRequestedBinding(
            Queue paymentRequestedQueue,
            DirectExchange billingExchange,
            @Value("${billing.rabbitmq.payment-requested-routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(paymentRequestedQueue).to(billingExchange).with(routingKey);
    }

    @Bean
    public Binding paymentCompletedBinding(
            Queue paymentCompletedQueue,
            DirectExchange billingExchange,
            @Value("${billing.rabbitmq.payment-completed-routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(paymentCompletedQueue).to(billingExchange).with(routingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        return factory;
    }
}
