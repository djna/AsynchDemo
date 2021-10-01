package org.djna.asynch;

import org.apache.activemq.ActiveMQConnectionFactory;


import javax.jms.*;


public class ExampleResponder implements MessageListener {
    private static int ackMode;
    private static String messageQueueName;
    private static String messageBrokerUrl;

    private Session session;
    private boolean transacted = false;
    private MessageProducer replyProducer;
    private MessageProtocol messageProtocol;

    static {
        messageBrokerUrl = "tcp://localhost:61616";
        messageQueueName = "client.messages";
        ackMode = Session.AUTO_ACKNOWLEDGE;
    }

    public ExampleResponder() {
         //Delegating the handling of messages to another class, instantiate it before setting up JMS so it
        //is ready to handle messages
        this.messageProtocol = new MessageProtocol();
        this.setupMessageQueueConsumer();
        System.out.printf("Connecting to %s, listening on %s%n", messageBrokerUrl, messageQueueName);
    }

    private void setupMessageQueueConsumer() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(messageBrokerUrl);
        Connection connection;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            this.session = connection.createSession(this.transacted, ackMode);
            Destination adminQueue = this.session.createQueue(messageQueueName);

            //Setup a message producer to respond to messages from clients, we will get the destination
            //to send to from the JMSReplyTo header field from a Message
            this.replyProducer = this.session.createProducer(null);
            this.replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            //Set up a consumer to consume messages off of the admin queue
            MessageConsumer consumer = this.session.createConsumer(adminQueue, "JMSPriority > 5");
            consumer.setMessageListener(this);
        } catch (JMSException e) {
            System.out.printf("Exception %s%n", e);
        }
    }

    public void onMessage(Message message) {
        try {
            System.out.printf("Received %s %n", message);
            TextMessage response = this.session.createTextMessage();
            if (message instanceof TextMessage) {
                TextMessage txtMsg = (TextMessage) message;
                String messageText = txtMsg.getText();
                System.out.printf("Replying to %s %n", messageText);
                response.setText(this.messageProtocol.handleProtocolMessage(messageText));
            }

            //Set the correlation ID from the received message to be the correlation id of the response message
            //this lets the client identify which message this is a response to if it has more than
            //one outstanding message to the server
            response.setJMSCorrelationID(message.getJMSCorrelationID());

            //Send the response to the Destination specified by the JMSReplyTo field of the received message,
            //this is presumably a temporary queue created by the client
            Destination replyTo = message.getJMSReplyTo();
            if ( replyTo == null){
                System.out.printf("No reply to specifed, cannot respond ");
            } else {
                this.replyProducer.send(replyTo, response);
            }

        } catch (JMSException e) {
            System.out.printf("Exception %s%n", e);
        }
    }

    public static void main(String[] args) {
        System.out.printf("Starting %n");
        new ExampleResponder();
    }
}