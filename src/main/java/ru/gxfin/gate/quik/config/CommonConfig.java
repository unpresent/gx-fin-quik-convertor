package ru.gxfin.gate.quik.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaAdmin;
import ru.gxfin.gate.quik.converter.QuikConverter;

import java.util.HashMap;

// @EnableConfigurationProperties({ConfigurationPropertiesServiceKafka.class, ConfigurationPropertiesServiceRedis.class})
public class CommonConfig {
    // -----------------------------------------------------------------------------------------------------------------
    // <editor-fold desc="Common">
    @Bean
    protected ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    // </editor-fold>
    // -----------------------------------------------------------------------------------------------------------------
    // <editor-fold desc="DbAdapter & Settings">
    @Bean
    protected QuikConverter dbAdapter(
            @Value("${service.name}") @NotNull final String serviceName,
            @Value("${service.provider-code}") @NotNull final String providerCode
    ) {
        return new QuikConverter(serviceName, providerCode);
    }
    // </editor-fold>
    // -----------------------------------------------------------------------------------------------------------------
    // <editor-fold desc="Kafka Common">
    @Value(value = "${service.kafka.server}")
    private String kafkaServer;

    @Bean
    protected KafkaAdmin kafkaAdmin() {
        final var configs = new HashMap<String, Object>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        return new KafkaAdmin(configs);
    }

    @Bean
    public KafkaIncomeTopicsConfiguration kafkaIncomeTopicsConfiguration() {
        return new KafkaIncomeTopicsConfiguration("kafka-income-config");
    }

    @Bean
    public KafkaOutcomeTopicsConfiguration kafkaOutcomeTopicsConfiguration() {
        return new KafkaOutcomeTopicsConfiguration("kafka-outcome-config");
    }

    @Bean
    public RedisIncomeCollectionsConfiguration redisIncomeCollectionsConfiguration() {
        return new RedisIncomeCollectionsConfiguration("redis-income-config");
    }
    // </editor-fold>
    // -----------------------------------------------------------------------------------------------------------------
}
