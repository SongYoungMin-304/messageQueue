package com.message.messagequeue.ActiveMq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;

@Slf4j
public class Sender {

    @Autowired
    private JmsTemplate jmsTemplate;

    public void send(String message) throws MalformedObjectNameException, ReflectionException, AttributeNotFoundException, InstanceNotFoundException, MBeanException, IOException {

        jmsTemplate.convertAndSend("mqTest.q",message);
    }
}
