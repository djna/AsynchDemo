package org.djna.asynch;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class TopicPublisher {

    public static void main(String[] args) throws Exception {
        startWork(makePublisher("home.thermostats"), false);
    }

    public static void startWork(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }

    public static Runnable makePublisher(String topic) {
        return new Runnable() {
            @Override

            public void run() {
                try {
                    ActiveMQConnectionFactory connectionFactory
                            = new ActiveMQConnectionFactory("tcp://localhost:61616");
                    Connection connection = connectionFactory.createConnection();
                    connection.start();
                    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                    // in ActiceMQ this will create a topic if it doesn't exist
                    Destination destination = session.createTopic(topic);

                    // Create a MessageProducer from the Session to the Topic or Queue
                    MessageProducer producer = session.createProducer(destination);
                    // TODO - set QOS options here

                    String text = "Hello world! From: "
                            + Thread.currentThread().getName() + " : " + this.hashCode();
                    TextMessage message = session.createTextMessage(text);

                    System.out.println("Sent message to "
                                + destination + ":"
                                + message.hashCode() + " : "
                                + Thread.currentThread().getName());
                    producer.send(message);

                    session.close();
                    connection.close();
                } catch (Exception e) {
                    System.out.println("Caught: " + e);
                    e.printStackTrace();
                }
            }
        };
    }
}