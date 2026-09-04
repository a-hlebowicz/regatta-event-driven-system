package pl.ahlebowicz.jury;

import org.springframework.boot.SpringApplication;

public class TestJuryApplication {

    public static void main(String[] args) {
        SpringApplication.from(JuryApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
