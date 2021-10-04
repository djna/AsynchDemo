package org.djna.asynch.intro;

import javax.jms.*;

public  class Receiver implements Runnable, MessageListener {

    protected Session session;
    protected String destination;

    public Receiver(Session session, String destination) {
        this.session = session;
        this.destination = destination;
    }

    public void run() {
        try {
            MessageConsumer consumer = session.createConsumer(session.createQueue(destination));
            System.out.printf("Receiving from %s%n", destination);
            consumer.setMessageListener(this);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    public void onMessage(Message message) {
        try {
            System.out.println(String.format("received message '%s' with message id '%s'", ((TextMessage) message).getText(), message.getJMSMessageID()));

            if ( ((TextMessage) message).getText().startsWith("bad")) {
                System.out.println("Commit, Message:" + message);
                session.rollback();
            } else {
                System.out.println("Rollback, Message:" + message);
                session.commit();
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
