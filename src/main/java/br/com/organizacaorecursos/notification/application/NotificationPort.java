package br.com.organizacaorecursos.notification.application;

public interface NotificationPort {

    void send(ReservationNotification notification);
}