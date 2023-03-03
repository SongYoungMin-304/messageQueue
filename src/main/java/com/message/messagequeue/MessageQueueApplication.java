package com.message.messagequeue;

import com.message.messagequeue.ActiveMq.Sender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MessageQueueApplication {


    public static void main(String[] args) {
        SpringApplication.run(MessageQueueApplication.class, args);
    }

}
