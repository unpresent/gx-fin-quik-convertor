package ru.gxfin.gate.quik.translator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import ru.gxfin.gate.quik.api.Translator;
import ru.gxfin.gate.quik.api.TranslatorLifeController;
import ru.gxfin.gate.quik.events.TranslatorStartEvent;
import ru.gxfin.gate.quik.events.TranslatorStopEvent;

@Slf4j
public class QuikTranslatorLifeController implements TranslatorLifeController {
    @Autowired
    private Translator provider;

    @EventListener(TranslatorStartEvent.class)
    @Override
    public void onEvent(TranslatorStartEvent event) {
        log.info("Starting onEvent(TranslatorStartEvent event)");
        this.provider.start();
        log.info("Finished onEvent(TranslatorStartEvent event)");
    }

    @EventListener(TranslatorStopEvent.class)
    @Override
    public void onEvent(TranslatorStopEvent event) {
        log.info("Starting onEvent(TranslatorStopEvent event)");
        this.provider.stop();
        log.info("Finished onEvent(TranslatorStopEvent event)");
    }
}
