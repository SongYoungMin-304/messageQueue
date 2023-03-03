package com.message.messagequeue;

import com.message.messagequeue.ActiveMq.Sender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @Autowired
    Sender sender;

    @GetMapping("/send")
    public String sendMethod() throws Exception{
        sender.send("test");
        return "test";
    }

}
