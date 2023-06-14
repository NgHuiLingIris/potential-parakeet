import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.common.serialization.Serdes;

import java.util.Properties;

public class KafkaEventConsumer {
    private final String topic;
    private final Properties props;
    private final String consumerGroup;

    public KafkaEventConsumer(String brokers, String username, String password, String realm, String consumerGroup) {
        this.topic = username + "-" + realm;
        this.consumerGroup = consumerGroup;
        String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
        String jaasCfg = String.format(jaasTemplate, username, password);
        String serializer = StringSerializer.class.getName();
        String deserializer = StringDeserializer.class.getName();
        props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("group.id", consumerGroup);
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "earliest");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", deserializer);
        props.put("value.deserializer", deserializer);
        props.put("key.serializer", serializer);
        props.put("value.serializer", serializer);
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.mechanism", "SCRAM-SHA-256");
        props.put("sasl.jaas.config", jaasCfg);
    }

    public void consume() {
        StreamsBuilder builder = new StreamsBuilder();

        // create a source stream from the Kafka topic
        KStream<String, String> sourceStream = builder.stream(topic);

        // define the processing topology, here we simply print the consumed records
        sourceStream.peek((key, value) -> System.out.printf("FROMSTREAM: %s, value=\"%s\"\n", key, value));

        // create a streams configuration properties map
        Properties streamProps = new Properties();
        streamProps.putAll(props);
        streamProps.put(StreamsConfig.APPLICATION_ID_CONFIG, consumerGroup);
        streamProps.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        streamProps.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        KafkaStreams streams = null;
        try {
            // build the topology and start streaming!
            streams = new KafkaStreams(builder.build(), streamProps);
            streams.start();
        } finally {
            if (streams != null) {
                // add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
                Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
            }
        }
    }

    public static void main(String[] args) {
        String realm = args[0];
        String brokers = args[1];
        String username = args[2];
        String password = args[3];
        String consumerGroup = args[4];
        KafkaEventConsumer c = new KafkaEventConsumer(brokers, username, password, realm, consumerGroup);
        c.consume();
    }
}
