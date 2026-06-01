package ma.atos.billing.invoice.billing_invoice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
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
    public DirectExchange billingDeadLetterExchange(@Value("${billing.rabbitmq.dead-letter-exchange}") String exchange) {
        return new DirectExchange(exchange);
    }

    @Bean
    public Queue paymentCompletedDeadLetterQueue(@Value("${billing.rabbitmq.payment-completed-dlq}") String queue) {
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
    public Binding paymentCompletedDeadLetterBinding(
            Queue paymentCompletedDeadLetterQueue,
            DirectExchange billingDeadLetterExchange,
            @Value("${billing.rabbitmq.payment-completed-dlq-routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(paymentCompletedDeadLetterQueue).to(billingDeadLetterExchange).with(routingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter,
            RabbitTemplate rabbitTemplate,
            @Value("${billing.rabbitmq.dead-letter-exchange}") String deadLetterExchange,
            @Value("${billing.rabbitmq.payment-completed-dlq-routing-key}") String deadLetterRoutingKey
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(retryInterceptor(rabbitTemplate, deadLetterExchange, deadLetterRoutingKey));
        return factory;
    }

    private Advice retryInterceptor(
            RabbitTemplate rabbitTemplate,
            String deadLetterExchange,
            String deadLetterRoutingKey
    ) {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 5000)
                .recoverer(new RepublishMessageRecoverer(rabbitTemplate, deadLetterExchange, deadLetterRoutingKey))
                .build();
    }
}
