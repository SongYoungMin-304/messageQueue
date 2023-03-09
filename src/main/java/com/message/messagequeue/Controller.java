package com.message.messagequeue;

import com.message.messagequeue.ActiveMq.Sender;
import com.message.messagequeue.Kafka.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
public class Controller {

    @Autowired
    Sender sender;

    @Autowired
    KafkaProducer producer;

    @GetMapping("/send")
    public String sendMethod() throws Exception{
        sender.send("test");
        return "test";
    }

    @GetMapping("/kafkaSend")
    public String sendMessage(@RequestParam("message") String message) throws ExecutionException, InterruptedException {
        producer.sendMessage(message);
        return "success";
    }


}
