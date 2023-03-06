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



------------------------------------------------------------------------------------------------

# kafka

------------------------------------------------------------------------------------------------

#kafka설치

### 1. **kafka 다운로드 받기 : [https://kafka.apache.org](https://kafka.apache.org/)**

(2.7.0 으로 설치해야 제대로 됬음)

### 2. kafka 파일 구성 확인하기

1) {kafka 폴더}**/bin/windows**: zookeeper, kafka 실행 bat파일 들어있습니다.

2) {kafka 폴더}**/config:** zookeeper, kafka config파일 들어있습니다.

### 3. zookeeper, kafka 실행시키기

**zookeeper 실행**

bin\windows\zookeeper-server-start.bat config\zookeeper.properties

**kafka 실행**

bin\windows\kafka-server-start.bat config\server.properties

![image](https://user-images.githubusercontent.com/56577599/222968064-347bb6bb-bf6d-40dd-a2ab-67df258e74a3.png)


- 프로듀서 : 카프카와 통신하면서 메시지를 보내는 역할
- 컨슈머 : 카프카와 통신하면서 메시지를 가져오는 역할
- 주키퍼 : 컨슈머와 통신, 카프카의 메타데이터 정보를 저장, 카프카의 상태관리 등 목적으로 이용
- 카프카

### 4. kafka topic 생성 하기

```bash
# kafka topic 생성하기
# bin\windows\kafka-topics.bat --create --bootstrap-server 카프카 접속주소:카프카 포트 --topic 카프카 토픽이름
ex)
bin\windows\kafka-topics.bat --create --bootstrap-server localhost:9092 --topic dev-topic

# kafka topic 생성확인
# bin\windows\kafka-topics.bat --list --bootstrap-server 카프카 접속주소:카프카 포트
ex)
bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092
```

** kafka topic이 생성되면 C:\tmp\kafka-logs 위치에도 topic의 폴더가 생성됩니다.

# kafka 설명

### kafka 란?

![image](https://user-images.githubusercontent.com/56577599/223131184-9d20f7be-3bd7-456d-b36f-0af01a0ce770.png)


- 메시징 큐의 하나로써 데이터 처리에 장점이 있음

- 프로듀서로부터 메시지를 전달 받고, 다시 이를 컨슈머로 전달하는 역할
- 카프카 메시지는 key(키) value(값)으로 구성
- 키는 해당 메시지가 카프카 브로커 내부에 저장될 때 저장되는 위치와 관련된 요소

### **토픽**

- 메시지를 구분하는 논리적인 단위

### **카프카 파티션**

- 브로커 내부의 물리적인 단위
- 모든 토픽은 각각 대응하는 하나 이상의 파티션이 브로커에 구성되고 발행되는 토픽 메시지들은 파티션에 나눠서 저장됩니다.

![Broker(서버) 3개,  토픽 T0, T1, 파티션 P0, P1, P2, P3
TO → 4개의 파티션(P0,P1,P2,P3)
T1 → 2개의 파티션(P0,P1)
]

![image](https://user-images.githubusercontent.com/56577599/223131255-de5e2374-17cd-4345-ac08-9b66a0326d38.png)


Broker(서버) 3개,  토픽 T0, T1, 파티션 P0, P1, P2, P3
TO → 4개의 파티션(P0,P1,P2,P3)
T1 → 2개의 파티션(P0,P1)

- Produce 에서 별다른 설정이 없으면 라운드 로빈 방식으로 파티션으로 나눠서 데이터를 보냄
**(분산 처리를 통한 성능 향상)
(서로 다른 브로커에 병렬 구성하여 요청의 부하를 분산시켜 줍니다.)**
- 하나의 파티션 내에서는 메시지 순서가 보장됨
- 다른 파티션에서는 순서 보장 안됨

### **파티션 복제**

- 하나의 파티션은 1개의 리더 레플리카와 그 외 0개 이상의 팔로어 레플리카로 구성됨
- 리더 레플리카가 파티션의 모든 쓰기, 읽기 작업을 담당합니다.
- 팔로어 레플리카는 리더 레플리카로 쓰인 메시지들을 그대로 복제
- 리더 레플리카에 장애가 발생하는 경우, 리더 자리를 승계받을 준비
- 리더 레플리카와 동기화된 레플리카들의 그룹을 ISR(In-Sync-Replica) 라고 함

![image](https://user-images.githubusercontent.com/56577599/223131310-50ef17a3-9075-401e-b108-c5353cfaac08.png)


### 윈도우에서 여러 브로커 띄우고 여러 파티션 및 레플리카 만들기

※ 원래는 ec2 서버를 열고 실제로 여러 서버에다가 진행해 볼까 했지만.. AWS를 잘못 사용해서 과금이 된 이후 무서워서 윈도우로 띄우려고 함..

**1) 주키퍼 구동(kafka 설치 참고)**

**2) 카프카 [server.properties](http://server.properties) 복제 및 구동**

```bash
server1.properties
server2.properties
server3.properties
```

server.1.properties

```xml
broker.id=1
listeners=PLAINTEXT://:9093
log.dirs=/tmp/kafka-logs1
```

server.2.properties

```xml
broker.id=2
listeners=PLAINTEXT://:9094
log.dirs=/tmp/kafka-logs2
```

server.3.properties

```xml
broker.id=3
listeners=PLAINTEXT://:9095
log.dirs=/tmp/kafka-logs3
```

실행 명령어

```xml
bin\windows\kafka-server-start.bat config\server1.properties
bin\windows\kafka-server-start.bat config\server2.properties
bin\windows\kafka-server-start.bat config\server3.properties
```

**3) Topics 생성**

```xml
bin\windows\kafka-topics.bat --create --topic my-kafka-topic --bootstrap-server localhost:9093 --partitions 4 --replication-factor 3
Created topic my-kafka-topic
```

**4) Topics 조회**

```xml
bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9093

// 결과
my-kafka-topic
```

```xml
bin\windows\kafka-topics.bat --describe --topic my-kafka-topic --bootstrap-server localhost:9093

// 결과
Topic: my-kafka-topic   PartitionCount: 4       ReplicationFactor: 3    Configs: segment.bytes=1073741824
        Topic: my-kafka-topic   Partition: 0    Leader: 2       Replicas: 2,3,1 Isr: 2,3,1
        Topic: my-kafka-topic   Partition: 1    Leader: 3       Replicas: 3,1,2 Isr: 3,1,2
        Topic: my-kafka-topic   Partition: 2    Leader: 1       Replicas: 1,2,3 Isr: 1,2,3
        Topic: my-kafka-topic   Partition: 3    Leader: 2       Replicas: 2,1,3 Isr: 2,1,3
```

5**) Topics 삭제**

kafka [server.properties](http://server.properties) 파일에 설정을 하고 kafka broker를 재기동해야 topic을 삭제할 수 있습니다.

```xml
# config/server.properties파일
delete.topic.enable = true
```

명령어

```xml
$  bin\windows\kafka-topics.bat --delete --topic my-kafka-topic --bootstrap-server localhost:9093
```

**6) Producers : Topic에 메시지 보내기**

```xml
bin\windows\kafka-console-producer.bat --broker-list localhost:9093,localhost:9094,localhost:9095 --topic my-kafka-topic
```

> first insert

> second insert

- 프로듀서가 데이터를 보낼때 “파티션키”를 지정해서 보낼 수 있습니다.
- 파티션키를 지정하지 않으면, 라운드로빈 방식으로 파티션에 저장
- 파티션키를 지정하면, 파티셔너가 파티션키의 HASH 값을 구해서 특정 파티션에 할당

- 카프카에서 kafka-console-producer.bat로 Consumer에게 메세지를 보낼 때 기본적으로 key값이 null로 설정되어 있습니다. 이럴 때 설정에서 parse.key 및 key.separator 속성을 설정하면 key와 value가 포함된 메시지를 보낼 수 있습니다.

```xml
$ bin\windows\kafka-console-producer.bat \
  --broker-list localhost:9093 \
  --topic my-kafka-topic \
  --property "parse.key=true" \
  --property "key.separator=:"
```

- parse.key : key와 value 파싱 활성화 여부
- key.separator : key와 value를 구분해주는 구분자
- print.key : 화면에 key 를 보여줄 지 여부를 지정
- print.value : 화면에 value 를 보여줄 지 여부를 지정

**7) Consumer : Topic 메시지 읽기**

```xml
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9093 --topic my-kafka-topic --from-beginning

```

first insert

second insert

- bootstrap-server는 클러스터의 브로커 중 하나일 수 있습니다.
- Topic은 생상자가 클러스터에 데이터를 입력한 Topic(주제)과 동일해야 합니다.
- from-beginning 은 클러스터에 현재 가지고 있는 모든 메시지를 원한다고 클러스터에 알립니다.
- 컨슈머 그룹이 다른 새로운 컨슈머가 auto.offset.reset=earliest 설정으로 데이터를 0번 부터 가져갈 수 있습니다. 설정하지 않으면 새롭게 토픽에 생성된 메세지만 읽어옵니다.

※KEY VALUE 로 표시하기

```xml
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9093 --topic my-kafka-topic --from-beginning \
   --property print.key=true --property key.separator=:
```

null:second insert

null:first insert

8**) Consumer 그룹**

```xml
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9093 --topic my-kafka-topic --group A그룹
```

```xml
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9093 --topic my-kafka-topic --group B그룹
```

- 그룹A를 두개 구동 시키고 그룹 B를 1개 구동
- 프로듀서가 TEST1, TEST2, TEST3, TEST4 를 입력

→ 그룹 A에서는 TEST1, TEST3 / TEST2. TEST4 이런 식으로 분배가 되고 

→ 그룹 B에서는 TEST1,TEST2,TEST3,TEST4 한번에 가져온다.

**※ KAFKA는 데이터를 CONSUME 이 가져간다고 해서 데이터가 사라지는 것이 아니라 위치를 기억할뿐**

9**) Offset**

```xml
bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9093 --group B그룹 --reset-offsets --shift-by -2 --execute --topic my-kafka-topic
```

→ 해당 명령어를 수행하고 B그룹에서 다시 CONSUMER 를 실행하면 offset위치를 옮겨주기 때문에 더 많은 데이터를 읽게 해준다.

**10) 브로커가 offline 되면?**

![image](https://user-images.githubusercontent.com/56577599/223131385-e18f002a-bf8a-4f3e-8a72-434299f9be4f.png)


- 신규 consumer 를 실행하면 데이터 손실 없이 그대로 읽어 올 수 있어야 한다.

※ 아래 에러 뜨면서 정상 동작 안함.. 확인 필요…

[2023-03-06 22:57:41,565] WARN [Consumer clientId=consumer-console-consumer-6004-1, groupId=console-consumer-6004] Connection to node 1 (host.docker.internal/172.30.1.22:9093) could not be established. Broker may not be available. (org.apache.kafka.clients.NetworkClient)
[2023-03-06 22:57:44,126] WARN [Consumer clientId=consumer-console-consumer-6004-1, groupId=console-consumer-6004] Connection to node 1 (host.docker.internal/172.30.1.22:9093) could not be established. Broker may not be available. (org.apache.kafka.clients.NetworkClient)
[2023-03-06 22:57:46,683] WARN [Consumer clientId=consumer-console-consumer-6004-1, groupId=console-consumer-6004] Connection to node 1 (host.docker.internal/172.30.1.22:9093) could not be established. Broker may not be available. (org.apache.kafka.clients.NetworkClient)
[2023-03-06 22:57:49,199] WARN [Consumer clientId=consumer-console-consumer-6004-1, groupId=console-consumer-6004] Connection to node 1 (host.docker.internal/172.30.1.22:9093) could not be established. Broker may not be available. (org.apache.kafka.clients.NetworkClient)
