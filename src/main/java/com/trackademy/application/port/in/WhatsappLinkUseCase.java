package com.trackademy.application.port.in;

import com.trackademy.domain.model.whatsapp.WhatsappLinkCodeResult;
import com.trackademy.domain.model.whatsapp.WhatsappLinkStatus;

public interface WhatsappLinkUseCase {

    WhatsappLinkCodeResult generateLinkCode(String email);

    WhatsappLinkStatus getLinkStatus(String email);

    void unlink(String email);
}
