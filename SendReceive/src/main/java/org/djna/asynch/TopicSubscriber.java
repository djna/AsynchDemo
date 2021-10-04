package org.djna.asynch;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class TopicSubscriber {

    public static void main(String[] args) throws Exception {
        thread(new HelloWorldConsumer(), false);
    }

    public static void thread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }

    public static class HelloWorldConsumer implements Runnable, ExceptionListener {
        public boolean stopping = false;
        public void run() {

            try {
                ActiveMQConnectionFactory connectionFactory
                        = new ActiveMQConnectionFactory("tcp://localhost:61616");
                Connection connection = connectionFactory.createConnection();
                connection.setClientID("HomeMonitor");
                connection.start();
                connection.setExceptionListener(this);
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Topic destination = session.createTopic("home.thermostats.>");
                MessageConsumer consumer = session.createDurableSubscriber(destination, "Thermostats");

                System.out.println("Subscribed: " + destination);
                while ( ! stopping ) {
                    Message message = consumer.receive(120 * 1000);

                    if (message instanceof TextMessage) {
                        TextMessage textMessage = (TextMessage) message;
                        String text = textMessage.getText();
                        System.out.println("Received: " + text);
                    } else {
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