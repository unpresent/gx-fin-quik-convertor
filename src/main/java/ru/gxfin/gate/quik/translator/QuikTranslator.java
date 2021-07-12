package ru.gxfin.gate.quik.translator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import ru.gxfin.common.worker.AbstractIterationExecuteEvent;
import ru.gxfin.common.worker.AbstractStartingExecuteEvent;
import ru.gxfin.common.worker.AbstractStoppingExecuteEvent;
import ru.gxfin.common.worker.AbstractWorker;
import ru.gxfin.gate.quik.events.TranslatorIterationExecuteEvent;
import ru.gxfin.gate.quik.events.TranslatorSettingsChangedEvent;
import ru.gxfin.gate.quik.events.TranslatorStartingExecuteEvent;
import ru.gxfin.gate.quik.events.TranslatorStoppingExecuteEvent;

@Slf4j
public class QuikTranslator extends AbstractWorker {
    // -----------------------------------------------------------------------------------------------------------------
    // <editor-fold desc="Fields">
    @Autowired
    private QuikTranslatorSettingsController settings;

    // </editor-fold>
    // -----------------------------------------------------------------------------------------------------------------
    // <editor-fold desc="Settings">
    @EventListener
    public void onEventChangedSettings(TranslatorSettingsChangedEvent event) {
        log.info("onEventChangedSettings({})", event.getSettingName());
    }

    @Override
    protected int getMinTimePerIterationMs() {
        return this.settings.getMinTimePerIterationMs();
    }

    @Override
    protected int getTimoutRunnerLifeMs() {
        return this.settings.getTimeoutLifeMs();
    }

    @Override
    public int getWaitOnStopMS() {
        return this.settings.getWaitOnStopMs();
    }

    @Override
    public int getWaitOnRestartMS() {
        return this.settings.getWaitOnRestartMS();
    }
    // </editor-fold>
    // -----------------------------------------------------------------------------------------------------------------

    public QuikTranslator() {
        super(QuikTranslator.class.getSimpleName());
    }

    @Override
    protected AbstractIterationExecuteEvent createIterationExecuteEvent() {
        return new TranslatorIterationExecuteEvent(this);
    }

    @Override
    protected AbstractStartingExecuteEvent createStartingExecuteEvent() {
        return new TranslatorStartingExecuteEvent(this);
    }

    @Override
    protected AbstractStoppingExecuteEvent createStoppingExecuteEvent() {
        return new TranslatorStoppingExecuteEvent(this);
    }

    @SuppressWarnings("unused")
    @EventListener(TranslatorStartingExecuteEvent.class)
    public void startingExecute(TranslatorStartingExecuteEvent event) {
        // TODO: ...
    }

    @SuppressWarnings("unused")
    @EventListener(TranslatorStoppingExecuteEvent.class)
    public void stoppingExecute(TranslatorStoppingExecuteEvent event) {
        // TODO: ...
    }

    @EventListener(TranslatorIterationExecuteEvent.class)
    public void iterationExecute(TranslatorIterationExecuteEvent event) {
        log.debug("Starting iterationExecute()");
        try {
            runnerIsLifeSet();

            // TODO: Тело!
        } catch (Exception e) {
            internalTreatmentExceptionOnDataRead(event, e);
        } finally {
            log.debug("Finished iterationExecute()");
        }
    }

    @SuppressWarnings("ImplicitArrayToString")
    private void internalTreatmentExceptionOnDataRead(TranslatorIterationExecuteEvent event, Exception e) {
        log.error(e.getMessage());
        log.error(e.getStackTrace().toString());
        if (e instanceof InterruptedException) {
            log.info("event.setStopExecution(true)");
            event.setStopExecution(true);
        } else {
            log.info("event.setNeedRestart(true)");
            event.setNeedRestart(true);
        }
    }
}
