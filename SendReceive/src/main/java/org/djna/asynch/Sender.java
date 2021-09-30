package org.djna.asynch;

import javax.jms.*;
import java.util.UUID;


public  class Sender implements Runnable {

    private Session session;
    private String destination;

    private int howManyToSend;
    private String messageText;
    private String responseDestination;
    private Requester responseReceiver;

    public Sender(Session initSession, String initDestination, int initHowManyToSend, String initText) {
        this.session = initSession;
        this.destination = initDestination;
        this.responseDestination = initDestination + "RespQ";
        howManyToSend = initHowManyToSend;
        messageText = initText;
    }

    public Sender(Session initSession, String initDestination ) {
        this(initSession, initDestination, 10, "a message" );
    }

    public void run() {
        try {
            MessageProducer messageProducer = session.createProducer(session.createQueue(destination));
            long counter = 0;

            while (counter < howManyToSend) {
                TextMessage message = session.createTextMessage(messageText + ":" + ++counter);
                message.setJMSMessageID(UUID.randomUUID().toString());
                message.setJMSCorrelationID("c:" + message.getText());
                messageProducer.send(message);
                System.out.printf("Sent %d: %s%n", counter, message);
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

}


