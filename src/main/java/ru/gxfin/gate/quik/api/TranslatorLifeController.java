package ru.gxfin.gate.quik.api;

import ru.gxfin.gate.quik.events.TranslatorStartEvent;
import ru.gxfin.gate.quik.events.TranslatorStopEvent;

/**
 * Обработка событий о запуске и останове (жизненый цикл) Провайдера
 * @author Vladimir Gagarkin
 * @since 1.0
 */
public interface TranslatorLifeController {

    /**
     * Обработчик команды о запуске провайдера
     * @param event команда о запуске провайдера
     */
    void onEvent(TranslatorStartEvent event);

    /**
     * Обработчик команды об остановке провайдера
     * @param event команда об остановке провайдера
     */
    void onEvent(TranslatorStopEvent event);
}
