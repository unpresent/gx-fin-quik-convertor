package ru.gxfin.gate.quik.config;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import ru.gx.core.channels.IncomeDataProcessType;
import ru.gx.core.redis.IncomeCollectionSortMode;
import ru.gx.core.redis.load.AbstractRedisIncomeCollectionsConfiguration;
import ru.gx.core.redis.upload.RedisOutcomeCollectionUploadingDescriptor;
import ru.gx.fin.common.fics.channels.FicsSnapshotCurrencyDataPublishChannelApiV1;
import ru.gx.fin.common.fics.channels.FicsSnapshotDerivativeDataPublishChannelApiV1;
import ru.gx.fin.common.fics.channels.FicsSnapshotSecurityDataPublishChannelApiV1;
import ru.gx.fin.gate.quik.provider.channels.QuikProviderSnapshotSecurityDataPublishChannelApiV1;

import javax.annotation.PostConstruct;

import static lombok.AccessLevel.PROTECTED;

public class RedisIncomeCollectionsConfiguration extends AbstractRedisIncomeCollectionsConfiguration {
    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private QuikProviderSnapshotSecurityDataPublishChannelApiV1 quikSecuritiesChannelApi;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private FicsSnapshotCurrencyDataPublishChannelApiV1 currencyDataPublishChannelApiV1;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private FicsSnapshotSecurityDataPublishChannelApiV1 securityDataPublishChannelApiV1;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private FicsSnapshotDerivativeDataPublishChannelApiV1 derivativeDataPublishChannelApiV1;

    public RedisIncomeCollectionsConfiguration(@NotNull String configurationName) {
        super(configurationName);
    }

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init() {
        this.getDescriptorsDefaults()
                .setSortMode(IncomeCollectionSortMode.None)
                .setProcessType(IncomeDataProcessType.SendToMessagesQueue);

        this
                .newDescriptor(this.currencyDataPublishChannelApiV1, RedisOutcomeCollectionUploadingDescriptor.class)
                .setPriority(0)
                .init();
        this
                .newDescriptor(this.securityDataPublishChannelApiV1, RedisOutcomeCollectionUploadingDescriptor.class)
                .setPriority(1)
                .init();
        this
                .newDescriptor(this.derivativeDataPublishChannelApiV1, RedisOutcomeCollectionUploadingDescriptor.class)
                .setPriority(2)
                .init();
        this
                .newDescriptor(this.quikSecuritiesChannelApi, RedisOutcomeCollectionUploadingDescriptor.class)
                .setPriority(3)
                .init();
    }
}
