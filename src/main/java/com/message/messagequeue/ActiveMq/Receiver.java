package com.message.messagequeue.ActiveMq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;

@Slf4j
public class Receiver {

    @JmsListener(destination = "mqTest.q",
            containerFactory = "customConnectionFactory")
    public void receive(String message) {
        log.info("received message='{}'", message);
    }

}
