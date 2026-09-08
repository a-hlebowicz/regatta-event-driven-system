package pl.ahlebowicz.jury.messaging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorHandling {

    private static final long RETRY_INTERVAL_MS = 3000L;

    // the default handler gives up after ten attempts, commits the offset and moves on,
    // here delivery is retried until it succeeds
    @Bean
    DefaultErrorHandler kafkaErrorHandler() {
        return new DefaultErrorHandler(new FixedBackOff(RETRY_INTERVAL_MS, FixedBackOff.UNLIMITED_ATTEMPTS));
    }
}
