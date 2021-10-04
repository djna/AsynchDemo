package org.djna.asynch.home.emulator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.djna.asynch.home.data.ThermostatReading;

import javax.jms.*;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

// Emulates Telemetry from multiple devices.
// Starts a thread per emualted device, each publishing to a separate topic
// and each publishing at a specified rate. Cycles through a range of values.
public class TelemetryEmulator {
    private static final Logger LOGGER = Logger.getLogger(TelemetryEmulator.class);
    private final static String baseTopic = "home.thermostats";

    public static void main(String[] args) throws Exception {
        // verify that logging is correctly configured in logback.xml
        LOGGER.error("Test Error");
        LOGGER.info("Starting");
        // usually don't enable to see this, debug from libraries is versbose
        LOGGER.debug("debug message");

        // example devices
        startWork(makeDevice("101","hall", 60), false);
        startWork(makeDevice("101","basement", 25), false);
    }

    // starts thread for specified emulator
    public static void startWork(Runnable deviceEmulator, boolean daemon) {
        Thread deviceThread = new Thread(deviceEmulator);
        deviceThread.setDaemon(daemon);
        deviceThread.start();
    }

    // Device factory
    public static Runnable makeDevice(String property, String location, final int frequencySeconds) {
        return new Runnable() {
            // each device establishes its own connection
            // as an enhancement we could start and stop them indepdently
            private ActiveMQConnectionFactory connectionFactory;
            private Connection connection;
            private Session session;
            private Destination destination;
            private MessageProducer producer;

            // currently we just run forever, but this is our shutdown flag
            private boolean stopping = false;

            @Override
            public void run() {
                try {
                   connectionFactory
                            = new ActiveMQConnectionFactory("tcp://localhost:61616");
                    Connection connection = connectionFactory.createConnection();
                    connection.start();
                    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                    // in ActiceMQ this will create a topic if it doesn't exist
                    String topic = MessageFormat.format(
                            "{0}.{1}.{2}", baseTopic, property, location);
                    destination = session.createTopic(topic);

                    // Create a MessageProducer from the Session to the Topic or Queue
                    producer = session.createProducer(destination);
                    // TODO - set QOS options here

                    int baseTemperature = 17;
                    int temperatureSkew = 0;

                    // TODO - add capability for clean shutdown
                    while (! stopping) {
                        publishTemperature(baseTemperature +temperatureSkew );

                        // prepare next values
                        temperatureSkew++;
                        temperatureSkew %= 15;

                        // good citizen check
                        int sleepFor =  frequencySeconds < 15 ? 15 : frequencySeconds;
                        TimeUnit.SECONDS.sleep(sleepFor);
                    }

                    session.close();
                    connection.close();
                } catch (Exception e) {
                    System.out.println("Caught: " + e);
                    e.printStackTrace();
                }
            }

            private void publishTemperature( int temperature ) throws JMSException, JsonProcessingException {
                ThermostatReading reading = new ThermostatReading(temperature, location);

                // publish jSON
                ObjectMapper mapper = new ObjectMapper();
                String text = mapper.writeValueAsString(reading);;
                TextMessage message = session.createTextMessage(text);

                System.out.println("Sent message to "
                        + destination + ":"
                        + message.getText() + " : "
                        + Thread.currentThread().getName());
                producer.send(message);
            }
        };
    }
}