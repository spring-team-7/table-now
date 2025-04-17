package org.example.tablenow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class TableNowApplication {

    public static void main(String[] args) {
        SpringApplication.run(TableNowApplication.class, args);
    }

}
