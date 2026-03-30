package org.example.numberguessinggame;

import org.example.numberguessinggame.configs.VnpayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(VnpayProperties.class)
public class NumberGuessingGameApplication {

    public static void main(String[] args) {
        SpringApplication.run(NumberGuessingGameApplication.class, args);
    }

}
