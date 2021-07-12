package ru.gxfin.gate.quik.api;

import ru.gxfin.gate.quik.errors.TranslatorException;
import ru.gxfin.gate.quik.errors.QuikConnectorException;
import ru.gxfin.gate.quik.events.TranslatorIterationExecuteEvent;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Управление каким-то одним потоком данных:
 * Запрос пакета данных у Connector-а, контроль за необходимостью немедленного повторения чения
 * @author Vladimir Gagarkin
 * @since 1.0
 */
public interface TranslatorDataController {
    void load(TranslatorIterationExecuteEvent iterationExecuteEvent) throws TranslatorException, IOException, QuikConnectorException, ExecutionException, InterruptedException;

    void clean();
}
