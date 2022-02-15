package ru.gxfin.gate.quik.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import ru.gx.core.channels.ChannelApiDescriptor;
import ru.gx.core.channels.ChannelDirection;
import ru.gx.core.data.DataObject;
import ru.gx.core.kafka.load.KafkaIncomeTopicsLoader;
import ru.gx.core.kafka.load.KafkaIncomeTopicsOffsetsController;
import ru.gx.core.kafka.offsets.TopicsOffsetsController;
import ru.gx.core.kafka.upload.KafkaOutcomeTopicUploadingDescriptor;
import ru.gx.core.kafka.upload.KafkaOutcomeTopicsUploader;
import ru.gx.core.messaging.DefaultMessagesFactory;
import ru.gx.core.messaging.Message;
import ru.gx.core.redis.load.RedisIncomeCollectionsLoader;
import ru.gx.core.simpleworker.SimpleWorker;
import ru.gx.core.simpleworker.SimpleWorkerOnIterationExecuteEvent;
import ru.gx.core.simpleworker.SimpleWorkerOnStartingExecuteEvent;
import ru.gx.core.simpleworker.SimpleWorkerOnStoppingExecuteEvent;
import ru.gx.fin.common.fics.messages.FicsSnapshotCurrencyDataPublish;
import ru.gx.fin.common.fics.messages.FicsSnapshotDerivativeDataPublish;
import ru.gx.fin.common.fics.messages.FicsSnapshotSecurityDataPublish;
import ru.gx.fin.common.fics.out.AbstractInstrument;
import ru.gx.fin.common.fics.out.Currency;
import ru.gx.fin.gate.quik.provider.messages.QuikProviderSnapshotSecurityDataPublish;
import ru.gx.fin.gate.quik.provider.messages.QuikProviderStreamAllTradesPackageDataPublish;
import ru.gx.fin.gate.quik.provider.messages.QuikProviderStreamDealsPackageDataPublish;
import ru.gx.fin.gate.quik.provider.messages.QuikProviderStreamOrdersPackageDataPublish;
import ru.gx.fin.gate.quik.provider.out.*;
import ru.gx.fin.md.channels.*;
import ru.gx.fin.md.dto.*;
import ru.gx.fin.md.messages.MdStreamTradeDataPublish;
import ru.gxfin.gate.quik.config.KafkaIncomeTopicsConfiguration;
import ru.gxfin.gate.quik.config.KafkaOutcomeTopicsConfiguration;
import ru.gxfin.gate.quik.config.RedisIncomeCollectionsConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PROTECTED;

@Slf4j
public class QuikConverter {
    // -----------------------------------------------------------------------------------------------------------------
    // <editor-fold desc="Fields">
    @Getter(PROTECTED)
    private final String serviceName;

    @Getter(PROTECTED)
    private final String providerCode;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private ObjectMapper objectMapper;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private SimpleWorker simpleWorker;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private KafkaIncomeTopicsConfiguration kafkaIncomeTopicsConfiguration;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private KafkaIncomeTopicsLoader kafkaIncomeTopicsLoader;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private KafkaIncomeTopicsOffsetsController kafkaIncomeTopicsOffsetsController;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private TopicsOffsetsController topicsOffsetsController;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private RedisIncomeCollectionsConfiguration redisIncomeCollectionsConfiguration;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private RedisIncomeCollectionsLoader redisIncomeCollectionsLoader;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private KafkaOutcomeTopicsConfiguration kafkaOutcomeTopicsConfiguration;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private KafkaOutcomeTopicsUploader kafkaOutcomeTopicsUploader;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private MdStreamTradeDataPublishChannelApiV1 tradeDataPublishChannelApiV1;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private MdStreamErrorTradeDataPublishChannelApiV1 errorTradeDataPublishChannelApiV1;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private MdStreamDealDataPublishChannelApiV1 dealDataPublishChannelApiV1;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private MdStreamErrorDealDataPublishChannelApiV1 errorDealDataPublishChannelApiV1;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private MdStreamOrderDataPublishChannelApiV1 orderDataPublishChannelApiV1;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private MdStreamErrorOrderDataPublishChannelApiV1 errorOrderDataPublishChannelApiV1;

    @Getter(PROTECTED)
    @Setter(value = PROTECTED, onMethod_ = @Autowired)
    private DefaultMessagesFactory messagesFactory;

    /**
     * Наполняется из потока QuikProviderSnapshotSecurityDataPublish
     */
    private final Map<String, QuikSecurity> quikInstrumentsIndex = new HashMap<>();

    /**
     * Наполняется из потоков:<br/>
     * - FicsSnapshotCurrencyDataPublish<br/>
     * - FicsSnapshotSecurityDataPublish<br/>
     * - FicsSnapshotDerivativeDataPublish<br/>
     */
    private final Map<String, AbstractInstrument> instrumentsIndex = new HashMap<>();

    /**
     * Наполняется из потока:<br/>
     * - FicsSnapshotCurrencyDataPublish
     */
    private final Map<String, Currency> currenciesIndex = new HashMap<>();
    // </editor-fold>
    // -----------------------------------------------------------------------------------------------------------------
    // <editor-fold desc="Initialization">
    public QuikConverter(@NotNull final String serviceName, @NotNull final String providerCode) {
        super();
        this.serviceName = serviceName;
        this.providerCode = providerCode;
    }
    // </editor-fold>
    // -----------------------------------------------------------------------------------------------------------------
    // <editor-fold desc="Обработка событий Worker-а">

    /**
     * Обработка события о начале работы цикла итераций.
     *
     * @param event Объект-событие с параметрами.
     */
    @SuppressWarnings("unused")
    @EventListener(SimpleWorkerOnStartingExecuteEvent.class)
    public void startingExecute(SimpleWorkerOnStartingExecuteEvent event) {
        log.debug("Starting startingExecute()");

        // Загрузка начальных данных из Redis
        loadRedisStartingData();

        // TODO: Надо убедиться, что справочники наполнились (все сообщения из Redis-а обработались).
        // Пока сделал Immediate при обработке сообщений из Redis-а.

        // Устанавливаем начальные смещения в Kafka
        initKafkaStartingOffsets();

        log.debug("Finished startingExecute()");
    }

    /**
     * Загрузка начальных данных из Redis
     */
    protected void loadRedisStartingData() {
        this.redisIncomeCollectionsLoader.processAllCollections(this.redisIncomeCollectionsConfiguration);
    }

    /**
     * Устанавливаем начальные смещения в Kafka
     */
    protected void initKafkaStartingOffsets() {
        final var offsets = this.topicsOffsetsController.loadOffsets(ChannelDirection.In, this.kafkaIncomeTopicsConfiguration.getConfigurationName());
        if (offsets.size() <= 0) {
            this.kafkaIncomeTopicsOffsetsController.seekAllToBegin(this.kafkaIncomeTopicsConfiguration);
        } else {
            this.kafkaIncomeTopicsOffsetsController.seekTopicsByList(this.kafkaIncomeTopicsConfiguration, offsets);
        }
    }

    /**
     * Обработка события об окончании работы цикла итераций.
     *
     * @param event Объект-событие с параметрами.
     */
    @SuppressWarnings("unused")
    @EventListener(SimpleWorkerOnStoppingExecuteEvent.class)
    public void stoppingExecute(SimpleWorkerOnStoppingExecuteEvent event) {
        log.debug("Starting stoppingExecute()");
        log.debug("Finished stoppingExecute()");
    }

    /**
     * Обработчик итераций.
     *
     * @param event Объект-событие с параметрами итерации.
     */
    @EventListener(SimpleWorkerOnIterationExecuteEvent.class)
    public void iterationExecute(SimpleWorkerOnIterationExecuteEvent event) {
        log.debug("Starting iterationExecute()");
        try {
            this.simpleWorker.runnerIsLifeSet();
            event.setImmediateRunNextIteration(false);

            try {
                // Загружаем данные и отправляем в очередь на обработку
                final var result = this.kafkaIncomeTopicsLoader
                        .processAllTopics(this.kafkaIncomeTopicsConfiguration);

                // Просто пишем в консоль статистику:
                for (var descriptor : result.keySet()) {
                    final var count = result.get(descriptor);
                    if (count > 1) {
                        log.debug("Loaded from {} {} records", descriptor.getApi().getName(), count);
                        event.setImmediateRunNextIteration(true);
                        break;
                    }
                }
            } catch (Exception e) {
                internalTreatmentExceptionOnDataRead(event, e);
            }

        } catch (Exception e) {
            internalTreatmentExceptionOnDataRead(event, e);
        } finally {
            log.debug("Finished iterationExecute()");
        }
    }

    /**
     * Обработка ошибки при выполнении итерации.
     *
     * @param event Объект-событие с параметрами итерации.
     * @param e     Ошибка, которую требуется обработать.
     */
    private void internalTreatmentExceptionOnDataRead(SimpleWorkerOnIterationExecuteEvent event, Exception e) {
        log.error("", e);
        if (e instanceof InterruptedException) {
            log.info("event.setStopExecution(true)");
            event.setStopExecution(true);
        } else {
            log.info("event.setNeedRestart(true)");
            event.setNeedRestart(true);
        }
    }
    // </editor-fold>
    // -----------------------------------------------------------------------------------------------------------------
    // <editor-fold desc="Обработка событий о чтении данных">

    protected void internalUploadObjects(
            @NotNull final List<? extends DataObject> dataObjects,
            @NotNull final ChannelApiDescriptor<? extends Message<?, ?>> channelApiDescriptor
    ) throws Exception {
        if (dataObjects.size() <= 0) {
            return;
        }
        final var outDescriptor = (KafkaOutcomeTopicUploadingDescriptor<Message<?, ?>>)
                this.kafkaOutcomeTopicsConfiguration
                        .get(channelApiDescriptor.getName());

        this.kafkaOutcomeTopicsUploader.uploadObjects(outDescriptor, dataObjects, null);
    }

    /**
     * 1) Получение {@link ru.gx.fin.gate.quik.provider.out.QuikAllTradesPackage}<br/>
     * 2) Конвертирование в список MdTrade,<br/>
     * 3) отправка (поштучно с упаковкой в исходящее сообщение) в исходящую очередь.<br/>
     *
     * @param message Сообщение с пакетом {@link ru.gx.fin.gate.quik.provider.out.QuikAllTradesPackage}.
     */
    @SneakyThrows({Exception.class})
    @EventListener(QuikProviderStreamAllTradesPackageDataPublish.class)
    public void processQuikAllTradesPackage(QuikProviderStreamAllTradesPackageDataPublish message) {
        log.debug("Starting processQuikAllTradesPackage()");
        try {
            final var resultObjects = new ArrayList<MdTrade>();
            final var errorObjects = new HashMap<QuikAllTrade, String>();

            internalProcessQuikAllTradesPackage(message.getBody().getDataPackage(), resultObjects, errorObjects);
            internalUploadObjects(resultObjects, this.tradeDataPublishChannelApiV1);
            internalUploadMdErrorsTrades(errorObjects);
        } finally {
            log.debug("Finished processQuikAllTradesPackage()");
        }
    }

    protected void internalProcessQuikAllTradesPackage(QuikAllTradesPackage sourcePackage, List<MdTrade> resultObjects, Map<QuikAllTrade, String> errors) {
        for (final var sourceObject : sourcePackage.getObjects()) {
            // TODO: Конвертируем QuikTrade -> MdTrade
            final var secId = sourceObject.getSecCode() + ":" + sourceObject.getClassCode();
            final var instrument = this.instrumentsIndex.get(sourceObject.getSecCode());
            final var quikInstrument = this.quikInstrumentsIndex.get(secId);
            final var currencyCode = QuikConverterUtils.getCurrencyCodeByQuikSecurity(quikInstrument);

            if (instrument == null) {
                errors.put(sourceObject, "Not found instrument by SecCode = " + sourceObject.getSecCode());
                continue;
            }
            if (currencyCode == null) {
                errors.put(sourceObject, "Not found currency for SecCode = " + sourceObject.getSecCode());
                continue;
            }

            final var mdTrade = new MdTrade(
                    sourceObject.getExchangeCode(), // -> placeCode
                    sourceObject.getTradeNum(),
                    QuikConverterUtils.getDealDirection(sourceObject.getDirection()),
                    sourceObject.getTradeDateTime(),
                    instrument.getGuid(),
                    currencyCode,
                    sourceObject.getPrice(),
                    sourceObject.getQuantity(),
                    sourceObject.getValue(),
                    sourceObject.getAccruedInterest(),
                    sourceObject.getYield(),
                    sourceObject.getRepoRate(),
                    sourceObject.getRepoValue(),
                    sourceObject.getRepo2Value(),
                    sourceObject.getRepoTerm(),
                    sourceObject.getPeriod(),
                    sourceObject.getOpenInterest()
            );
            resultObjects.add(mdTrade);
        }
    }

    protected void internalUploadMdErrorsTrades(Map<QuikAllTrade, String> errors) throws Exception {
        final var objects = new ArrayList<MdErrorTrade>();
        if (errors.size() > 0) {
            for (final var errorObjectEntry : errors.entrySet()) {
                final var errorTrade = new MdErrorTrade(
                        errorObjectEntry.getKey().getExchangeCode(),
                        errorObjectEntry.getKey().getTradeNum(),
                        errorObjectEntry.getKey().getTradeDateTime(),
                        errorObjectEntry.getValue()
                );
                objects.add(errorTrade);
            }

            internalUploadObjects(objects, this.errorTradeDataPublishChannelApiV1);
        }
    }

    /**
     * 1) Получение {@link ru.gx.fin.gate.quik.provider.out.QuikOrdersPackage}<br/>
     * 2) Конвертирование в список MdTrade,<br/>
     * 3) отправка (поштучно с упаковкой в исходящее сообщение) в исходящую очередь.
     *
     * @param message Сообщение с пакетом {@link ru.gx.fin.gate.quik.provider.out.QuikOrdersPackage}.
     */
    @SneakyThrows({Exception.class})
    @EventListener(QuikProviderStreamOrdersPackageDataPublish.class)
    public void processQuikOrdersPackage(QuikProviderStreamOrdersPackageDataPublish message) {
        log.debug("Starting processQuikOrdersPackage()");
        try {
            final var resultObjects = new ArrayList<MdOrder>();
            final var errorObjects = new HashMap<QuikOrder, String>();

            internalProcessQuikOrdersPackage(message.getBody().getDataPackage(), resultObjects, errorObjects);
            internalUploadObjects(resultObjects, this.orderDataPublishChannelApiV1);
            internalUploadMdErrorsOrders(errorObjects);
        } finally {
            log.debug("Finished processQuikOrdersPackage()");
        }
    }

    protected void internalProcessQuikOrdersPackage(QuikOrdersPackage sourcePackage, List<MdOrder> resultObjects, Map<QuikOrder, String> errors) {
        for (final var sourceObject : sourcePackage.getObjects()) {
            // TODO: Конвертируем QuikTrade -> MdTrade
            final var secId = sourceObject.getSecCode() + ":" + sourceObject.getClassCode();
            final var instrument = this.instrumentsIndex.get(sourceObject.getSecCode());
            final var quikInstrument = this.quikInstrumentsIndex.get(secId);
            final var currencyCode = QuikConverterUtils.getCurrencyCodeByQuikSecurity(quikInstrument);

            if (instrument == null) {
                errors.put(sourceObject, "Not found instrument by SecCode = " + sourceObject.getSecCode());
                continue;
            }
            if (currencyCode == null) {
                errors.put(sourceObject, "Not found currency for SecCode = " + sourceObject.getSecCode());
                continue;
            }

            final var mdOrder = new MdOrder(
                    sourceObject.getExchangeCode(), // -> placeCode
                    sourceObject.getOrderNum(),
                    QuikConverterUtils.getDealDirection(sourceObject.getDirection()),
                    sourceObject.getTradeDateTime(),
                    instrument.getGuid(),
                    currencyCode,
                    sourceObject.getPrice(),
                    sourceObject.getQuantity(),
                    sourceObject.getValue(),
                    sourceObject.getAccruedInterest(),
                    sourceObject.getYield(),
                    sourceObject.getRepoValue(),
                    sourceObject.getRepo2Value(),
                    sourceObject.getRepoTerm()
            );
            resultObjects.add(mdOrder);
        }
    }

    protected void internalUploadMdErrorsOrders(Map<QuikOrder, String> errors) throws Exception {
        final var objects = new ArrayList<MdErrorOrder>();
        if (errors.size() > 0) {
            for (final var errorObjectEntry : errors.entrySet()) {
                final var errorOrder = new MdErrorOrder(
                        errorObjectEntry.getKey().getExchangeCode(),
                        errorObjectEntry.getKey().getOrderNum(),
                        errorObjectEntry.getValue()
                );
                objects.add(errorOrder);
            }

            internalUploadObjects(objects, this.errorOrderDataPublishChannelApiV1);
        }
    }

    /**
     * 1) Получение {@link ru.gx.fin.gate.quik.provider.out.QuikDealsPackage}<br/>
     * 2) Конвертирование в список MdTrade,<br/>
     * 3) отправка (поштучно с упаковкой в исходящее сообщение) в исходящую очередь.<br/>
     *
     * @param message Сообщение с пакетом {@link ru.gx.fin.gate.quik.provider.out.QuikDealsPackage}.
     */
    @SneakyThrows({Exception.class})
    @EventListener(QuikProviderStreamDealsPackageDataPublish.class)
    public void processQuikDealsPackage(QuikProviderStreamDealsPackageDataPublish message) {
        log.debug("Starting processQuikDealsPackage()");
        try {
            final var resultObjects = new ArrayList<MdDeal>();
            final var errorObjects = new HashMap<QuikDeal, String>();

            internalProcessQuikDealsPackage(message.getBody().getDataPackage(), resultObjects, errorObjects);
            internalUploadObjects(resultObjects, this.orderDataPublishChannelApiV1);
            internalUploadMdErrorsDeals(errorObjects);
        } finally {
            log.debug("Finished processQuikDealsPackage()");
        }
    }

    protected void internalProcessQuikDealsPackage(QuikDealsPackage sourcePackage, List<MdDeal> resultObjects, Map<QuikDeal, String> errors) {
        for (final var sourceObject : sourcePackage.getObjects()) {
            // TODO: Конвертируем QuikDeal -> MdDeal
            final var secId = sourceObject.getSecCode() + ":" + sourceObject.getClassCode();
            final var instrument = this.instrumentsIndex.get(sourceObject.getSecCode());
            final var quikInstrument = this.quikInstrumentsIndex.get(secId);
            final var currencyCode = QuikConverterUtils.getCurrencyCodeByQuikSecurity(quikInstrument);

            if (instrument == null) {
                errors.put(sourceObject, "Not found instrument by SecCode = " + sourceObject.getSecCode());
                continue;
            }
            if (currencyCode == null) {
                errors.put(sourceObject, "Not found currency for SecCode = " + sourceObject.getSecCode());
                continue;
            }

            final var mdDeal = new MdDeal(
                    sourceObject.getExchangeCode(), // -> placeCode
                    sourceObject.getTradeNum(),
                    sourceObject.getOrderNum(),
                    QuikConverterUtils.getDealDirection(sourceObject.getDirection()),
                    sourceObject.getTradeDateTime(),
                    instrument.getGuid(),
                    currencyCode,
                    sourceObject.getPrice(),
                    sourceObject.getQuantity(),
                    sourceObject.getValue(),
                    sourceObject.getAccruedInterest(),
                    sourceObject.getYield(),
                    sourceObject.getRepoRate(),
                    sourceObject.getRepoValue(),
                    sourceObject.getRepo2Value(),
                    sourceObject.getRepoTerm(),
                    sourceObject.getPeriod()
            );
            resultObjects.add(mdDeal);
        }
    }

    protected void internalUploadMdErrorsDeals(Map<QuikDeal, String> errors) throws Exception {
        final var objects = new ArrayList<MdErrorDeal>();
        if (errors.size() > 0) {
            for (final var errorObjectEntry : errors.entrySet()) {
                final var errorDeal = new MdErrorDeal(
                        errorObjectEntry.getKey().getExchangeCode(),
                        errorObjectEntry.getKey().getTradeNum(),
                        errorObjectEntry.getKey().getTradeDateTime(),
                        errorObjectEntry.getValue()
                );
                objects.add(errorDeal);
            }

            internalUploadObjects(objects, this.errorDealDataPublishChannelApiV1);
        }
    }

    /**
     * 1) Получение {@link ru.gx.fin.common.fics.out.Currency}<br/>
     * 2) Сохранение в памяти для дальнейшего использования при обработке потоковых данных.<br/>
     *
     * @param message Сообщение с {@link ru.gx.fin.common.fics.out.Currency}.
     */
    @SneakyThrows({Exception.class})
    @EventListener(FicsSnapshotCurrencyDataPublish.class)
    public void processFicsCurrency(FicsSnapshotCurrencyDataPublish message) {
        log.debug("Starting processFicsCurrency()");
        try {
            final var sourceObject = message.getBody().getDataObject();
            this.currenciesIndex.put(sourceObject.getCodeAlpha3(), sourceObject);
            this.instrumentsIndex.put(sourceObject.getCodeAlpha3(), sourceObject);
        } finally {
            log.debug("Finished processFicsCurrency()");
        }
    }

    /**
     * 1) Получение {@link ru.gx.fin.common.fics.out.Security}<br/>
     * 2) Сохранение в памяти для дальнейшего использования при обработке потоковых данных.<br/>
     *
     * @param message Сообщение с {@link ru.gx.fin.common.fics.out.Security}.
     */
    @SneakyThrows({Exception.class})
    @EventListener(FicsSnapshotSecurityDataPublish.class)
    public void processFicsSecurity(FicsSnapshotSecurityDataPublish message) {
        log.debug("Starting processFicsSecurity()");
        try {
            final var sourceObject = message.getBody().getDataObject();
            sourceObject.getCodes()
                    .stream()
                    .filter(c -> this.providerCode.equals(c.getProvider()))
                    .forEach(instrumentCode -> this.instrumentsIndex.put(instrumentCode.getCode(), sourceObject));
        } finally {
            log.debug("Finished processFicsSecurity()");
        }
    }

    /**
     * 1) Получение {@link ru.gx.fin.common.fics.out.Security}<br/>
     * 2) Сохранение в памяти для дальнейшего использования при обработке потоковых данных.<br/>
     *
     * @param message Сообщение с {@link ru.gx.fin.common.fics.out.Security}.
     */
    @SneakyThrows({Exception.class})
    @EventListener(FicsSnapshotDerivativeDataPublish.class)
    public void processFicsDerivative(FicsSnapshotDerivativeDataPublish message) {
        log.debug("Starting processFicsDerivative()");
        try {
            final var sourceObject = message.getBody().getDataObject();
            sourceObject.getCodes()
                    .stream()
                    .filter(c -> this.providerCode.equals(c.getProvider()))
                    .forEach(instrumentCode -> this.instrumentsIndex.put(instrumentCode.getCode(), sourceObject));
        } finally {
            log.debug("Finished processFicsDerivative()");
        }
    }

    /**
     * 1) Получение {@link QuikSecurity}<br/>
     * 2) Сохранение в памяти для дальнейшего использования при обработке потоковых данных.<br/>
     *
     * @param message Сообщение с {@link ru.gx.fin.common.fics.out.Currency}.
     */
    @SneakyThrows({Exception.class})
    @EventListener(QuikProviderSnapshotSecurityDataPublish.class)
    public void processSnapshotQuikSecurity(QuikProviderSnapshotSecurityDataPublish message) {
        log.debug("Starting processSnapshotQuikSecurity()");
        try {
            final var sourceObject = message.getBody().getDataObject();
            final var inMemObject = this.quikInstrumentsIndex.get(sourceObject.getId());
            if (inMemObject == null || inMemObject.getActualDate().isBefore(sourceObject.getActualDate())) {
                this.quikInstrumentsIndex.put(sourceObject.getId(), sourceObject);
            }
        } finally {
            log.debug("Finished processSnapshotQuikSecurity()");
        }

        // TODO: Что делать с обновлением ?
    }
    // </editor-fold>
    // -----------------------------------------------------------------------------------------------------------------
}
