package pl.ahlebowicz.office;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class HealthEndpointTest {

    @Autowired
    private MockMvcTester mockMvc;

    @Test
    void health_reportsDatabaseAndBroker() {
        assertThat(mockMvc.get().uri("/actuator/health")).hasStatusOk().bodyJson().isLenientlyEqualTo("""
                {
                  "status": "UP",
                  "components": {
                    "db": { "status": "UP" },
                    "kafka": { "status": "UP" }
                  }
                }
                """);
    }
}
