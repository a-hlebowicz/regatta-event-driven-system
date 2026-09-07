package pl.ahlebowicz.office.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class OfficeTopics {

    public static final String OFFICE_EVENTS = "regatta.office.events";

    @Bean
    NewTopic officeEvents() {
        return TopicBuilder.name(OFFICE_EVENTS)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
