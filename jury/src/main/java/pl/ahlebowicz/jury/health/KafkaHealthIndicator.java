package pl.ahlebowicz.jury.health;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.Node;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Component
public class KafkaHealthIndicator extends AbstractHealthIndicator implements DisposableBean {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final Admin admin;

    public KafkaHealthIndicator(KafkaAdmin kafkaAdmin) {
        this.admin = Admin.create(kafkaAdmin.getConfigurationProperties());
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        // KafkaAdmin.clusterId() caches its answer and would keep reporting UP after the broker is gone
        DescribeClusterResult cluster = admin.describeCluster(new DescribeClusterOptions().timeoutMs((int) TIMEOUT.toMillis()));
        Collection<Node> nodes = cluster.nodes().get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);

        builder.up()
                .withDetail("clusterId", cluster.clusterId().get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS))
                .withDetail("nodes", nodes.size());
    }

    @Override
    public void destroy() {
        admin.close(TIMEOUT);
    }
}
