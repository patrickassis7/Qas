package br.com.organizacaorecursos.notification.infrastructure;

import br.com.organizacaorecursos.notification.application.NotificationPort;
import br.com.organizacaorecursos.notification.application.ReservationNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

@Component
public class HttpNotificationAdapter implements NotificationPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpNotificationAdapter.class);

    private final RestClient restClient;

    public HttpNotificationAdapter(
            @Value("${app.notification.base-url:http://localhost:8099}") String baseUrl,
            @Value("${app.notification.connect-timeout-ms:500}") long connectTimeoutMs,
            @Value("${app.notification.read-timeout-ms:500}") long readTimeoutMs
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    @Override
    public void send(ReservationNotification notification) {
        try {
            restClient.post()
                    .uri("/notifications")
                    .body(notification)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            LOGGER.warn("Notificação simulada falhou para a reserva {} ({}).",
                    notification.reservationId(), exception.getClass().getSimpleName());
        }
    }
}