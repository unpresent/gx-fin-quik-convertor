package ru.gxfin.gate.quik.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import ru.gx.core.kafka.load.AbstractKafkaIncomeTopicsConfiguration;
import ru.gx.core.kafka.load.KafkaIncomeTopicLoadingDescriptor;
import ru.gx.fin.gate.quik.provider.channels.QuikProviderStreamAllTradesPackageDataPublishChannelApiV1;
import ru.gx.fin.gate.quik.provider.channels.QuikProviderStreamDealsPackageDataPublishChannelApiV1;
import ru.gx.fin.gate.quik.provider.channels.QuikProviderStreamOrdersPackageDataPublishChannelApiV1;
import ru.gx.fin.gate.quik.provider.channels.QuikProviderStreamSecuritiesPackageDataPublishChannelApiV1;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Properties;

import static lombok.AccessLevel.PROTECTED;

public class KafkaIncomeTopicsConfiguration extends AbstractKafkaIncomeTopicsConfiguration {
    @Value("${service.name}")
    private String serviceName;

    @Value(value = "${service.kafka.server}")
    private String kafkaServer;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private QuikProviderStreamAllTradesPackageDataPublishChannelApiV1 allTradesChannelApi;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private QuikProviderStreamDealsPackageDataPublishChannelApiV1 dealsChannelApi;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private QuikProviderStreamOrdersPackageDataPublishChannelApiV1 ordersChannelApi;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private QuikProviderStreamSecuritiesPackageDataPublishChannelApiV1 securitiesChannelApi;

    public KafkaIncomeTopicsConfiguration(@NotNull String configurationName) {
        super(configurationName);
    }

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init() {
        this.getDescriptorsDefaults()
                .setDurationOnPoll(Duration.ofMillis(25))
                .setPartitions(0)
                .setConsumerProperties(consumerProperties());

        this
                .newDescriptor(this.ordersChannelApi, KafkaIncomeTopicLoadingDescriptor.class)
                .setPriority(0)
                .init();
        this
                .newDescriptor(this.dealsChannelApi, KafkaIncomeTopicLoadingDescriptor.class)
                .setPriority(1)
                .init();
        this
                .newDescriptor(this.securitiesChannelApi, KafkaIncomeTopicLoadingDescriptor.class)
                .setPriority(2)
                .init();
        this
                .newDescriptor(this.allTradesChannelApi, KafkaIncomeTopicLoadingDescriptor.class)
                .setPriority(3)
                .init();
    }

    public Properties consumerProperties() {
        final var result = new Properties();
        result.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        result.put(ConsumerConfig.GROUP_ID_CONFIG, this.serviceName);
        result.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        result.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        result.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return result;
    }
}
