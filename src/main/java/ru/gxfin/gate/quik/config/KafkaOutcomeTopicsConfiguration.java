package ru.gxfin.gate.quik.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import ru.gx.core.kafka.load.AbstractKafkaIncomeTopicsConfiguration;
import ru.gx.core.kafka.load.KafkaIncomeTopicLoadingDescriptor;
import ru.gx.core.kafka.upload.AbstractKafkaOutcomeTopicsConfiguration;
import ru.gx.fin.gate.quik.provider.channels.QuikProviderStreamAllTradesPackageDataPublishChannelApiV1;
import ru.gx.fin.gate.quik.provider.channels.QuikProviderStreamDealsPackageDataPublishChannelApiV1;
import ru.gx.fin.gate.quik.provider.channels.QuikProviderStreamOrdersPackageDataPublishChannelApiV1;
import ru.gx.fin.gate.quik.provider.channels.QuikProviderStreamSecuritiesPackageDataPublishChannelApiV1;
import ru.gx.fin.md.channels.MdStreamDealDataPublishChannelApiV1;
import ru.gx.fin.md.channels.MdStreamOrderDataPublishChannelApiV1;
import ru.gx.fin.md.channels.MdStreamTradeDataPublishChannelApiV1;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Properties;

import static lombok.AccessLevel.PROTECTED;

public class KafkaOutcomeTopicsConfiguration extends AbstractKafkaOutcomeTopicsConfiguration {
    @Value(value = "${service.kafka.server}")
    private String kafkaServer;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private MdStreamTradeDataPublishChannelApiV1 tradeDataPublishChannelApiV1;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private MdStreamDealDataPublishChannelApiV1 dealDataPublishChannelApiV1;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private MdStreamOrderDataPublishChannelApiV1 orderDataPublishChannelApiV1;

    public KafkaOutcomeTopicsConfiguration(@NotNull String configurationName) {
        super(configurationName);
    }

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init() {
        this.getDescriptorsDefaults()
                .setProducerProperties(producerProperties());

        this
                .newDescriptor(this.orderDataPublishChannelApiV1, KafkaIncomeTopicLoadingDescriptor.class)
                .init();
        this
                .newDescriptor(this.dealDataPublishChannelApiV1, KafkaIncomeTopicLoadingDescriptor.class)
                .init();
        this
                .newDescriptor(this.tradeDataPublishChannelApiV1, KafkaIncomeTopicLoadingDescriptor.class)
                .init();
    }


    protected Properties producerProperties() {
        final var props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }
}
