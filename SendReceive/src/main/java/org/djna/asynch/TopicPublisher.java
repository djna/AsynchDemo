package org.djna.asynch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.catalina.filters.RemoteIpFilter;
import org.apache.log4j.Logger;
import org.djna.asynch.homedata.ThermostatReading;

import javax.jms.*;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

public class TopicPublisher {
    private static final Logger LOGGER = Logger.getLogger(TopicPublisher.class);
    private final static String baseTopic = "home.thermostats";

    public static void main(String[] args) throws Exception {
        LOGGER.error("Test Error");
        LOGGER.info("Starting");
        LOGGER.debug("debug message");
        startWork(makePublisher("101","hall", 60), false);
        startWork(makePublisher("101","basement", 25), false);
    }

    public static void startWork(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }

    public static Runnable makePublisher(String property, String location, final int frequencySeconds) {
        return new Runnable() {
            private ActiveMQConnectionFactory connectionFactory;
            private Connection connection;
            private Session session;
            private Destination destination;
            private MessageProducer producer;

            private boolean stopping = false;


            @Override
            public void run() {

                try {
                   connectionFactory
                            = new ActiveMQConnectionFactory("tcp://localhost:61616");
                    Connection connection = connectionFactory.createConnection();
                    connection.start();
                    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                    // in ActiceMQ this will create a topic if it doesn't exist
                    String topic = MessageFormat.format(
                            "{0}.{1}.{2}", baseTopic, property, location);
                    destination = session.createTopic(topic);

                    // Create a MessageProducer from the Session to the Topic or Queue
                    producer = session.createProducer(destination);
                    // TODO - set QOS options here

                    int baseTemperature = 17;
                    int temperatureSkew = 0;

                    // TODO - add capability for clean shutdown
                    while (! stopping) {
                        publishTemperature(baseTemperature +temperatureSkew );
                        temperatureSkew++;
                        temperatureSkew %= 15;

                        // good citizen check
                        int sleepFor =  frequencySeconds < 15 ? 15 : frequencySeconds;

                        TimeUnit.SECONDS.sleep(sleepFor);
                    }

                    session.close();
                    connection.close();
                } catch (Exception e) {
                    System.out.println("Caught: " + e);
                    e.printStackTrace();
                }
            }

            private void publishTemperature( int temperature ) throws JMSException, JsonProcessingException {
                ThermostatReading reading = new ThermostatReading(temperature, location);
                ObjectMapper mapper = new ObjectMapper();
                String text = mapper.writeValueAsString(reading);;
                TextMessage message = session.createTextMessage(text);

                System.out.println("Sent message to "
                        + destination + ":"
                        + message.getText() + " : "
                        + Thread.currentThread().getName());
                producer.send(message);
            }
        };
    }
}