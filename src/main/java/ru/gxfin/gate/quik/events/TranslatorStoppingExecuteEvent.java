package ru.gxfin.gate.quik.events;

import ru.gxfin.common.worker.AbstractStoppingExecuteEvent;

public class TranslatorStoppingExecuteEvent extends AbstractStoppingExecuteEvent {
    public TranslatorStoppingExecuteEvent(Object source) {
        super(source);
    }
}
