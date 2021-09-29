package org.djna.asynch;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

public class ResponseReceiver extends Receiver {
    private Sender sender;
    public ResponseReceiver(Session session, String destination, Sender theSender) {
        super(session, destination);
        sender = theSender;
    }


    public void onMessage(Message message) {
        try {
            System.out.println(String.format("received message '%s' with message id '%s'", ((TextMessage) message).getText(), message.getJMSMessageID()));

            try {
                sender.processResponse(message);
                System.out.println("Processed, Message:" + message);
                session.commit();
            } catch( Exception e){
                System.out.println("Exception, Message:" + message);
                session.rollback();
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}


