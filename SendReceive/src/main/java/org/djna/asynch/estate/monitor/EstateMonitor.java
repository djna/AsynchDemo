package org.djna.asynch.estate.monitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.djna.asynch.estate.data.ThermostatReading;

import javax.jms.*;
import java.util.ArrayList;
import java.util.List;

// Estate Monitor
// Subscribes to all topics and displays each message
// Starting point for adding logic to detect problems.
public class EstateMonitor {

    public static void main(String[] args) throws Exception {
        thread(new Monitor(), false);
    }

    public static void thread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }

    // Implement two interfaces, so need a class.
    // Used only here, so private inner class.
    private static class Monitor implements Runnable, ExceptionListener {
        public boolean stopping = false;
        public void run() {

            try {
                ActiveMQConnectionFactory connectionFactory
                        = new ActiveMQConnectionFactory("tcp://localhost:61616");
                Connection connection = connectionFactory.createConnection();
                connection.setClientID("EstateMonitor");
                connection.start();
                connection.setExceptionListener(this);
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Topic destination = session.createTopic("home.thermostats.>");
                MessageConsumer consumer = session.createDurableSubscriber(destination, "Thermostats");
                ObjectMapper mapper = new ObjectMapper();


                System.out.println("Subscribed: " + destination);

                while ( ! stopping ) {
                    // Using the polling API as an example
                    // Using the Asynch API would probably be better
                    Message message = consumer.receive(120 * 1000);

                    // expecting only Text Message
                    if (message instanceof TextMessage) {
                        TextMessage textMessage = (TextMessage) message;
                        String text = textMessage.getText();
                        ThermostatReading reading = mapper.readValue(text, ThermostatReading.class);
                        System.out.println("Received: " + reading);
                    } else {
                        // but handle the unexpected
                        System.out.println("Received: " + message);
                    }
                }

                consumer.close();
                session.close();
                connection.close();
            } catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }

        public synchronized void onException(JMSException ex) {
            System.out.println("JMS Exception occured.  Shutting down client.");
        }
    }
}