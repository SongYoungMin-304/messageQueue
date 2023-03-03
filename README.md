# messageQueue

# Active Mq

메세지 큐를 사용하는 이유

→ 기존의 동기식 통신 방식은 사용자로부터 받은 요청을 전부 처리할 때까지 Blocking 
→ 비동기 처리 방식으로 큐에 던지고 큐에서 가져가도록 처리

![image](https://user-images.githubusercontent.com/56577599/222684819-c16333b0-96c6-4d70-a49c-8f1dec151930.png)


1) **Jms(Java Message Service) 클라이언트와 함께 자바로 작성된 오픈 소스 메시지 브로커**

**자바 기반의 MOM API 표준, 둘 이상의 클라이언트 간의 메세지를 보낸다.**

2) 메시지 생성, 송수신, 읽기를 한다.

3) ActiveMq의 jms 라이브러리를 사용한 자바 어플리케이션간 통신 가능

### 메세지 큐의 장점

- 비동기(Asynchronous) : 큐에 데이터를 넣어놓음으로써 필요시 꺼내 쓸 수 있다.
- 비동조(Decoupling) : 어플리케이션과 분리할 수 있다.
- 탄력성(Resilience) : 일부가 실패해도 전체에 영향을 주지 않는다.
- 과잉(Redundancy) : 실패하더라도 재실행이 가능하다.
- 보증(Guarantees) : 작업이 처리되었는지 확인이 가능하다.
- 확장성(Scalable) : 다수의 프로세스들이 큐에 메세지를 보낼 수 있다.

### 어느 경우에 사용해야 할까?

- 대용량 데이터 처리를 위한 배치 작업
- 비동기 데이터 처리
- 채팅

### ActiveMQ vs RabbitMq vs Kafka

RabbitMq : AMQP 프로토콜을 구현해 놓은 오픈소스

ActiveMq : jms로 구현 자바 특화

Kafka : 대용량 실시간 로그 처리에 특화되어 설계된 메시징 시스템(TPS 매우 우수)

## ACTIVE MQ 사용법(WITH SPRING BOOT)

## 1) activeMq bean 세팅

```xml
spring:
  activemq:
    broker-url: tcp://127.0.0.1:61616?jms.useAsyncSend=true&wireFormat.tightEncodingEnabled=false
    user: admin
    password: admin
```

```java
@Configuration
public class JmsConfiguration {

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    /*
     URL을 통해서 ActiveMQConnectionFactory 생성
     */
    
    public ActiveMQConnectionFactory senderActiveMQConnectionFactory() {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
        activeMQConnectionFactory.setBrokerURL(brokerUrl);
        return activeMQConnectionFactory;
    }

    /*
     ActiveMQConnectionFactory 통해서 CachingConnectionFactory
     */
   
    public CachingConnectionFactory cachingConnectionFactory() {
        return new CachingConnectionFactory(senderActiveMQConnectionFactory());
    }

    /*
     cachingConnectionFactory 통해서 jmsTemplate 세팅
     */
    @Bean
    public JmsTemplate jmsTemplate(){
        return new JmsTemplate(cachingConnectionFactory());
    }

    @Bean
    public Sender sender(){
        return new Sender();
    }

    // Receiver
    
    public ActiveMQConnectionFactory receiverActiveMQConnectionFactory() {
        ActiveMQConnectionFactory activeMQConnectionFactory =
                new ActiveMQConnectionFactory();
        activeMQConnectionFactory.setBrokerURL(brokerUrl);

        return activeMQConnectionFactory;
    }

    @Bean(name = "customConnectionFactory")
    public JmsListenerContainerFactory jmsListenerContainerFactory() {
        DefaultJmsListenerContainerFactory factory =
                new DefaultJmsListenerContainerFactory();
        factory
                .setConnectionFactory(receiverActiveMQConnectionFactory());

        return factory;
    }

    @Bean
    public Receiver receiver() {
        return new Receiver();
    }
```

**Sender**

→ activeMqConnectionFactory  등록 메소드 정의

→ activeMqConnectionFactory 등록 메소드 정의 통해 CachingConnectionFactory 등록메소드

→ CachingConnectionFactory 등록메소드를 통해서 jmsTemplate Bean 등록

**→ jmsTemplate을 쓰는 Sender Bean 등록**

```java
@Slf4j
public class Sender {

    @Autowired
    private JmsTemplate jmsTemplate;

    public void send(String message) throws MalformedObjectNameException, ReflectionException, AttributeNotFoundException, InstanceNotFoundException, MBeanException, IOException {

        jmsTemplate.convertAndSend("mqTest.q",message);
    }
}
```

**Receiver**

→ activeMqConnectionFactory  등록 메소드 정의

→ activeMqConnectionFactory 등록 메소드 정의 통해 JmsListenerContainerFactory 등록메소드

→ CachingConnectionFactory 등록메소드를 통해서 JmsListenerContainerFactory Bean 등록

**→ JmsListenerContainerFactory 을 쓰는 Receiver Bean 등록**

```java
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
```

## 2) activeMq 사용

```java
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
```

실행결과

**1) activemq 구동**

→ [http://localhost:8161/admin/queues.jsp](http://localhost:8161/admin/queues.jsp)(admin/admin)

![image](https://user-images.githubusercontent.com/56577599/222684914-156b49fb-c32e-444d-9fb7-a17abb718dd2.png)


**2) activemq sender 호출(http://localhost:8080/send)**

→ 해당 queue에 데이터가 들어가는 것을 확인

3**) activemq receiver 호출**

→ jms listener 통해서 queue 에 있는 데이터를 읽어서 처리

*** jmx를 통해서 queue의 갯수를 가져와서 분산 처리를 해보려고 했으나 적절한 예제를 찾지못함..**

## 3) activeMq 활용

activemq가 존재하는 서버가 내려가는 경우를 대비해서 이중화 처리가 가능하다.

activemq 는 active standby 방식을 통한 이중화를 제공함

한마디로 같은 KahaDB를 볼 수 있게 세팅을 진행하고

   1번 ActiveMq 를 구동하고(port:8163, 61618)

    2번 ActiveMq 를 구동한다면(port:8164, 61619)

**프로세스는 두개 떠 있지만 포트는 하나만 Listen 처리가 됩니다.**

**activemq.xml** 

1번

```xml
<broker xmlns="http://activemq.apache.org/schema/core" brokerName="test01" dataDirectory="${activemq.data}">

<transportConnector name="nio" uri="nio://0.0.0.0:61618?maximumConnections=10000&amp;wireFormat.maxFrameSize=104857600"/>
```

2번

```xml
<broker xmlns="http://activemq.apache.org/schema/core" brokerName="test02" dataDirectory="${activemq.data}">

<transportConnector name="nio" uri="nio://0.0.0.0:61619?maximumConnections=10000&amp;wireFormat.maxFrameSize=104857600"/>
```

=> brokerName 을 다르게 세팅 하고 포트를 세팅합니다

```xml
<persistenceAdapter>

            <kahaDB directory="/app/projects/tms_bccard/data/activemq/kahadb"  indexWriteBatchSize="1000" enableIndexWriteAsync="true" enableJournalDiskSyncs="false"/>

</persistenceAdapter>
```

**jetty.xml**

1번

```xml
<bean id="jettyPort" class="org.apache.activemq.web.WebConsolePort" init-method="start">

             <!-- the default port number for the web console -->

        <property name="host" value="0.0.0.0"/>

        <property name="port" value="8163"/>

    </bean>
```

2번

```xml
<bean id="jettyPort" class="org.apache.activemq.web.WebConsolePort" init-method="start">

             <!-- the default port number for the web console -->

        <property name="host" value="0.0.0.0"/>

        <property name="port" value="8164"/>

    </bean>
```

→ 해당 세팅으로 activeMq를 설정해 놓고 activeMq 2개를 구동 시키면 먼저 켜는 것이 Master 나중에 켜지는 것이 Slave

( 해당 Url([http://localhost:8163/](http://localhost:8163/)) 을 통해서 구동을 확인 가능)

→ 해당 상황은 프로세스는 두개가 떠있으나(test01, test02) 포트는 Master 부분만 올라가 있음

해당 상황에서 Master ActiveMq 를 죽이게 된다면 Slave 가 Master 로 변경 되어서 포트가 Listen 되는 것을 볼수 있음

**추가) Master Slave , ActiveMq 를 사용 하는 url**

failover:(tcp://localhost:61618,tcp://localhost:61619)?randomize=false

- > randomize=false로 세팅을 해야지 랜덤으로 가져오지 않고 하나가 안되면 하나를 가져오는 형식으로 처리됩니다.

(randomize=true 는 로드 밸런싱입니다.)
