package ru.gxfin.gate.quik.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import ru.gxfin.gate.quik.api.TranslatorLifeController;
import ru.gxfin.gate.quik.api.TranslatorSettingsController;
import ru.gxfin.gate.quik.connector.QuikConnector;
import ru.gxfin.gate.quik.translator.QuikTranslator;
import ru.gxfin.gate.quik.translator.QuikTranslatorLifeController;
import ru.gxfin.gate.quik.translator.QuikTranslatorSettingsController;

public class CommonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    @SneakyThrows
    @Bean
    @Autowired
    public QuikTranslatorSettingsController quikTranslatorSettings(ApplicationContext context) {
        return new QuikTranslatorSettingsController(context);
    }

    @Bean
    @Autowired
    public QuikConnector connector(TranslatorSettingsController settings) {
        return new QuikConnector(settings.getQuikPipeName(), settings.getBufferSize());
    }

    @Bean
    public QuikTranslator translator() {
        return new QuikTranslator();
    }

    @Bean
    public TranslatorLifeController translatorLifeController() {
        return new QuikTranslatorLifeController();
    }
}
