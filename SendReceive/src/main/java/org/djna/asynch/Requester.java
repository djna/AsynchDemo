package org.djna.asynch;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class Requester implements Runnable, MessageListener {
    private String sendDestination;
    private MessageProducer messageProducer;
    private ActiveMQConnectionFactory connFact;
    private Session receiverSession;
    private static String clientId = "RequestSender";
    private static String requestQueueName = "ask";
    private static String responseQueueName = "askRespQ";
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].length() > 0 ){
            requestQueueName = args[0];
        }

        Requester inquistor = new Requester(requestQueueName);
    }

    private Sender sender;

    public Requester(String destination) throws JMSException {

        requestQueueName = destination;
        responseQueueName = destination+"RespQ";
        connFact = new ActiveMQConnectionFactory("tcp://localhost:61616");
        connFact.setConnectResponseTimeout(10000);

        {
            Connection conn = connFact.createConnection("admin", "admin");
            conn.setClientID(clientId+"Sender");
            conn.start();
            Session receiverSession = conn.createSession(true,
                    Session.CLIENT_ACKNOWLEDGE);
            Sender theSender = new Sender(receiverSession, requestQueueName,
                    1, "Answer me this");
            sender = theSender;
            new Thread(theSender).start();
        }

        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            Connection conn = connFact.createConnection("admin", "admin");
            conn.setClientID(clientId);
            conn.start();
            receiverSession = conn.createSession(true,
                    Session.SESSION_TRANSACTED);
            MessageConsumer consumer = receiverSession.createConsumer(
                              receiverSession.createQueue(responseQueueName));
            System.out.printf("Receiving from %s%n", responseQueueName);
            consumer.setMessageListener(this);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            System.out.println(String.format("received message '%s' with message id '%s'", ((TextMessage) message).getText(), message.getJMSMessageID()));

            if ( ((TextMessage) message).getText().startsWith("bad")) {
                System.out.println("Rollback, Message:" + message);
                receiverSession.rollback();
            } else {
                System.out.println("Commit, Message:" + message);
                receiverSession.commit();
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

}


