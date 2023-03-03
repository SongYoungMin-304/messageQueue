package com.message.messagequeue.config;

import com.message.messagequeue.ActiveMq.Receiver;
import com.message.messagequeue.ActiveMq.Sender;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jmx.support.MBeanServerConnectionFactoryBean;

import javax.management.MBeanServerConnection;
import java.net.MalformedURLException;

@Configuration
public class JmsConfiguration {

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    /*
     URL을 통해서 ActiveMQConnectionFactory 생성
     */
    //@Bean
    public ActiveMQConnectionFactory senderActiveMQConnectionFactory() {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
        activeMQConnectionFactory.setBrokerURL(brokerUrl);
        return activeMQConnectionFactory;
    }

    /*
     ActiveMQConnectionFactory 통해서 CachingConnectionFactory
     */
    //@Bean
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
    @Bean
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

    /*@Bean
    public FactoryBean<MBeanServerConnection> mbeanServerConnection() throws MalformedURLException {
        MBeanServerConnectionFactoryBean mBeanServerConnectionFactoryBean = new MBeanServerConnectionFactoryBean();
        mBeanServerConnectionFactoryBean
                .setServiceUrl(brokerUrl);
        return mBeanServerConnectionFactoryBean;
    }*/

}
