package ru.gxfin.gate.quik.translator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.gxfin.common.settings.AbstractSettingsController;
import ru.gxfin.common.settings.UnknownApplicationSettingException;
import ru.gxfin.gate.quik.api.TranslatorSettingsController;

@Slf4j
public class QuikTranslatorSettingsController extends AbstractSettingsController implements TranslatorSettingsController {
    @Autowired
    public QuikTranslatorSettingsController(ApplicationContext context) throws UnknownApplicationSettingException {
        super(context);

        // TODO: Переписать на чтение настроек
        loadStringSetting(QUIK_PIPE_NAME);
        loadIntegerSetting(BUFFER_SIZE);
        loadIntegerSetting(ATTEMPTS_ON_CONNECT);
        loadIntegerSetting(PAUSE_ON_CONNECT_MS);
        loadIntegerSetting(WAIT_ON_STOP_MS);
        loadIntegerSetting(WAIT_ON_RESTART_MS);
        loadIntegerSetting(MIN_TIME_PER_ITERATION_MS);
        loadIntegerSetting(INTERVAL_MANDATORY_READ_STATE_MS);
        loadIntegerSetting(TIMEOUT_LIFE_MS);

        loadStringSetting(INCOME_TOPIC_ALLTRADES);
        loadStringSetting(INCOME_TOPIC_DEALS);
        loadStringSetting(INCOME_TOPIC_ORDERS);
        loadStringSetting(INCOME_TOPIC_SECURITIES);
    }

    @Override
    public String getQuikPipeName() {
        return (String) this.getSetting(QUIK_PIPE_NAME);
    }

    @Override
    public int getBufferSize() {
        return (Integer) this.getSetting(BUFFER_SIZE);
    }

    @Override
    public int getAttemptsOnConnect() {
        return (Integer) this.getSetting(ATTEMPTS_ON_CONNECT);
    }

    @Override
    public int getPauseOnConnectMs() {
        return (Integer) this.getSetting(PAUSE_ON_CONNECT_MS);
    }

    @Override
    public int getWaitOnStopMs() {
        return (Integer) this.getSetting(WAIT_ON_STOP_MS);
    }

    @Override
    public int getWaitOnRestartMS() {
        return (Integer) this.getSetting(WAIT_ON_RESTART_MS);
    }

    @Override
    public int getMinTimePerIterationMs() {
        return (Integer) this.getSetting(MIN_TIME_PER_ITERATION_MS);
    }

    @Override
    public int getIntervalMandatoryReadStateMs() {
        return (Integer) this.getSetting(INTERVAL_MANDATORY_READ_STATE_MS);
    }

    @Override
    public int getTimeoutLifeMs() {
        return (Integer) this.getSetting(TIMEOUT_LIFE_MS);
    }

    @Override
    public String getOutcomeTopicAlltrades() {
        return (String) this.getSetting(INCOME_TOPIC_ALLTRADES);
    }

    @Override
    public String getOutcomeTopicDeals() {
        return (String) this.getSetting(INCOME_TOPIC_DEALS);
    }

    @Override
    public String getOutcomeTopicOrders() {
        return (String) this.getSetting(INCOME_TOPIC_ORDERS);
    }

    @Override
    public String getOutcomeTopicSecurities() {
        return (String) this.getSetting(INCOME_TOPIC_SECURITIES);
    }
}
