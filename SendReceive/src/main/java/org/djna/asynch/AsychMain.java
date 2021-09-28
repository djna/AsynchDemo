package org.djna.asynch;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;

import javax.jms.Connection;
import javax.jms.Session;

public class AsychMain {
    public static void main(String[] args) throws Exception {
        String clientId = "Example";
        String queueName = "Queue.PointToPoint.OneWay.Traditional";
        if (args.length >= 1){
            clientId = args[0];
        }
        System.out.printf("Queue Name %s%n", clientId);

        if (args.length > 2){
            queueName = args[1];
        }
        System.out.printf("ClientId %s%n", queueName);

        ActiveMQConnectionFactory connFact = new ActiveMQConnectionFactory("tcp://localhost:61616");
        connFact.setConnectResponseTimeout(10000);
        Connection conn = connFact.createConnection("admin", "admin");
        conn.setClientID(clientId);
        conn.start();

        int howManyToSend = 5;
        String messageText = "Message from sender";
        boolean receive = true;

        if (args.length >= 3) {
            try {
                String[] sendConfigItems = args[2].split("=");
                if (sendConfigItems.length > 1){
                    messageText = sendConfigItems[1];
                }
                howManyToSend = Integer.valueOf(sendConfigItems[0]);
            } catch (NumberFormatException nfe) {
                usageExit("Number of messages to send, not a valid number");
            }
        }
        if (args.length >= 4) {
            receive = ("receive".equalsIgnoreCase(args[3]));
        }

        if (howManyToSend > 0) {
            System.out.printf("Sending %d messages {%s} %n", howManyToSend, messageText);
            new Thread(new Sender(
                    conn.createSession(false, Session.CLIENT_ACKNOWLEDGE),
                    queueName,
                    howManyToSend,
                    messageText)
                  ).start();

        }
        if ( receive) {
            new Thread(new Receiver(conn.createSession(true,
                    Session.SESSION_TRANSACTED),
                    queueName)).start();
        }
    }

    private static void usageExit(String message) {
        System.out.println(message);
        System.out.println("Argument 1: a number of messages to send, 0 for none, default 5");
        System.out.println("Optional second argument: 'receive' will start a receiver, other values will not, default to receive");

    }

}
