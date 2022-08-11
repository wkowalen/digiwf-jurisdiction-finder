package de.muenchen.digiwf.jurisdictionfinder.streaming;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.muenchen.digiwf.jurisdictionfinder.service.ZmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.client.model.InlineObject26;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageProcessor {
    private final ZmsService zmsService;

    //private final CorrelateMessageService correlateMessageService;

    private static final String APPOINTMENT_CONFIRMED_STATUS = "appointmentConfirmedStatus";

    @KafkaListener(topics = "#{'${spring.kafka.topic}'}", groupId = "#{'${spring.kafka.consumer.group-id}'}")

    // Method
    public void
    confirmAppointmentFromEventBus_AlternateImplementation(String message)
    {
        // Print statement
        System.out.println("message = " + message);
        ObjectMapper myObjectMapper = new ObjectMapper();
        //System.out.println(new File(".").getAbsolutePath());
        //System.out.println(new File(".").getPath());
        InlineObject26 myObject = new InlineObject26();
        //myObject = myObjectMapper.readValue(new File("eappointment-digiwf-integration/src/main/resources/test-data.json"), InlineObject26.class);
        zmsService.confirmAppointment(myObject);

    }


    @Bean
    public Consumer<Message<InlineObject26>> confirmAppointmentFromEventBus() {
        return message -> {
            log.info("Processing new appointment from eventbus");
            InlineObject26 process = message.getPayload();
            log.debug("Appointment: {}", process);
            try {
                zmsService.confirmAppointment(process);
                emitResponse(message.getHeaders(), true);
            } catch (final RestClientException e) {
                log.error("Appointment could not be confirmed: {}", e.getMessage());
                emitResponse(message.getHeaders(), false);
            }
        };
    }

    public void emitResponse(final MessageHeaders messageHeaders, final boolean status) {
        final Map<String, Object> correlatePayload = new HashMap<>();
        correlatePayload.put(APPOINTMENT_CONFIRMED_STATUS, status);
        //correlateMessageService.sendCorrelateMessage(messageHeaders, correlatePayload);
    }
}
