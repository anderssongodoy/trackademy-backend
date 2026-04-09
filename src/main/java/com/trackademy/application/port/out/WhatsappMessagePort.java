package com.trackademy.application.port.out;

public interface WhatsappMessagePort {

    void sendTextMessage(String to, String body);
}
