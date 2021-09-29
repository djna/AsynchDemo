package org.djna.asynch;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.UUID;
import javax.jms.Connection;

/*
 * Listens on a request queue and sends correlated responses
 */
public  class ResponseSender extends Receiver
{

    private Session session;
    private String sendDestination;
    private MessageProducer messageProducer;
    private static String clientId = "ResponseSender";
    private static String requestQueueName = "ask";

    public static void main(String[] args) throws Exception{
        if (args.length > 0 && args[0].length() > 0 ){
            requestQueueName = args[0];
        }
        ActiveMQConnectionFactory connFact = new ActiveMQConnectionFactory("tcp://localhost:61616");
        connFact.setConnectResponseTimeout(10000);
        Connection conn = connFact.createConnection("admin", "admin");
        conn.setClientID(clientId);
        conn.start();
        Session session = conn.createSession(true,
                                    Session.SESSION_TRANSACTED);
        new Thread(new ResponseSender(session,
                                      requestQueueName)).start();
    }
    public ResponseSender(Session initSession, String receiveDestination) throws JMSException {
        super(initSession, receiveDestination );
        session = initSession;

        sendDestination = receiveDestination+"RespQ";
        System.out.printf("Listen on %s, reply to %s", receiveDestination, sendDestination);

        messageProducer = session.createProducer(session.createQueue(sendDestination));
    }

    public void sendResponse(TextMessage receivedMessage) throws JMSException {
        String receivedText = receivedMessage.getText();
        TextMessage message = session.createTextMessage( "response:" + receivedText + ":no");
        message.setJMSMessageID(UUID.randomUUID().toString());
        message.setJMSCorrelationID(message.getJMSMessageID());
        messageProducer.send(message);
        System.out.printf("Sent  %s%n", message);
    }

    public void onMessage(Message message) {
        try {
            System.out.println(String.format("received message '%s' with message id '%s'", ((TextMessage) message).getText(), message.getJMSMessageID()));

            try {
                sendResponse((TextMessage)message);
                System.out.println("Commit, Message:" + message);
                session.commit();
            } catch(Exception e) {
                System.out.println("Rollback :" + e);
                session.rollback();
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}


