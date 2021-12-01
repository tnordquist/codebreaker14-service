package edu.cnm.deepdive.configuration;

import org.apache.commons.rng.simple.JDKRandomBridge;
import org.apache.commons.rng.simple.RandomSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
public class BeanProvider {

    @Bean
    public Random getRandom() {
        return new JDKRandomBridge(RandomSource.XO_RO_SHI_RO_128_PP, null);
    }
}
