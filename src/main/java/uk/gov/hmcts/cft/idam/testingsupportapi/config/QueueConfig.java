package uk.gov.hmcts.cft.idam.testingsupportapi.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import io.opentelemetry.api.trace.Span;
import jakarta.jms.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import uk.gov.hmcts.cft.idam.testingsupportapi.error.ListenerErrorHandler;
import uk.gov.hmcts.cft.idam.testingsupportapi.trace.TraceAttribute;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@EnableJms
@Configuration
@Slf4j
public class QueueConfig {

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {

        JavaTimeModule timeModule = new JavaTimeModule();
        timeModule.addSerializer(ZonedDateTime.class,
                                 new ZonedDateTimeSerializer(DateTimeFormatter.ISO_ZONED_DATE_TIME)
        );

        ObjectMapper objectMapper = new ObjectMapper();
        // default settings for MappingJackson2MessageConverter
        objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(timeModule);

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setObjectMapper(objectMapper);

        return converter;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                          ListenerErrorHandler errorHandler,
                                                                          MessageConverter messageConverter,
                                                                          @Value("${cleanup.concurrency:1-3}")
                                                                              String cleanupConcurrency) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setErrorHandler(errorHandler);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrency(cleanupConcurrency);
        factory.setExceptionListener(e -> {
            Span.current().setAttribute(TraceAttribute.ERROR, "exception: " + e.getClass() + ": " + e.getMessage());
            log.info("JMS Listener Exception: {}: {}", e.getClass(), e.getMessage(), e);
        });
        return factory;
    }

}
