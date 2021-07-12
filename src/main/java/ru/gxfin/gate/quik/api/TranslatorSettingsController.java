package ru.gxfin.gate.quik.api;

public interface TranslatorSettingsController {
    String QUIK_PIPE_NAME = "quik.pipe_name";
    String BUFFER_SIZE = "quik.buffer_size";
    String ATTEMPTS_ON_CONNECT = "quik.attempts_on_connect";
    String PAUSE_ON_CONNECT_MS = "quik.pause_on_connect_ms";
    String WAIT_ON_STOP_MS = "translator.wait_on_stop_ms";
    String WAIT_ON_RESTART_MS = "translator.wait_on_restarts_ms";
    String MIN_TIME_PER_ITERATION_MS = "translator.min_time_per_iteration_ms";
    String INTERVAL_MANDATORY_READ_STATE_MS = "translator.interval_mandatory_read_state_ms";
    String TIMEOUT_LIFE_MS = "translator.timeout_life_ms";

    String INCOME_TOPIC_ALLTRADES = "kafka.income_topic.all_trades";
    String INCOME_TOPIC_DEALS = "kafka.income_topic.deals";
    String INCOME_TOPIC_ORDERS = "kafka.income_topic.orders";
    String INCOME_TOPIC_SECURITIES = "kafka.income_topic.securities";

    String getQuikPipeName();
    int getBufferSize();
    int getAttemptsOnConnect();
    int getPauseOnConnectMs();
    int getWaitOnStopMs();
    int getWaitOnRestartMS();

    int getMinTimePerIterationMs();
    int getIntervalMandatoryReadStateMs();
    int getTimeoutLifeMs();

    String getOutcomeTopicAlltrades();
    String getOutcomeTopicDeals();
    String getOutcomeTopicOrders();
    String getOutcomeTopicSecurities();
}
