package com.trackademy.application.port.out;

import com.trackademy.domain.model.whatsapp.WspResponse;

public interface WhatsappMessagePort {

    void send(String to, WspResponse response);
}
