package com.message.messagequeue.Kafka;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    @Value(value ="${message.topic.name}")
    private String topicName;

    private final KafkaTemplate<String, String> kafkaTemplate;


    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message){
        System.out.println("message print" + message);
        this.kafkaTemplate.send(topicName, message);
    }
}
