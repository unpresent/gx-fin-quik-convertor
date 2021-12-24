package ru.gxfin.gate.quik;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import ru.gx.core.messaging.DoStartStandardMessagesExecutorEvent;
import ru.gx.core.simpleworker.DoStartSimpleWorkerEvent;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        final var context = new SpringApplicationBuilder(Application.class)
                // .web(WebApplicationType.NONE)
                .run(args);
        DoStartStandardMessagesExecutorEvent.publish(context, context);
        DoStartSimpleWorkerEvent.publish(context, context);
    }
}
