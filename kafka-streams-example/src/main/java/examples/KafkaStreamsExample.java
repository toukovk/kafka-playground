package examples;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Properties;

/**
 * Simple Kafka Streams example that
 * - consumes an input topic
 * - maps messages (calculates string length messages)
 * - filters messages (requiring at least certain length)
 * - produces the result to an output topic
 */
public class KafkaStreamsExample {

    private static final String INPUT_TOPIC = "string-topic";
    private static final String OUTPUT_TOPIC = "string-length-topic";
    private static final int MIN_LENGTH = 5;

    private static StreamsConfig buildConfig() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-streams-foo");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new StreamsConfig(props);
    }

    public static void main(String[] args) {
        Serde<String> stringSerde = Serdes.String();

        StreamsBuilder streamsBuilder = new StreamsBuilder();

        streamsBuilder.stream(INPUT_TOPIC, Consumed.with(stringSerde, stringSerde))
                .mapValues(String::length)
                .filter((key, length) -> length >= MIN_LENGTH)
                .mapValues(length -> Integer.toString(length))
                .to(OUTPUT_TOPIC, Produced.with(stringSerde, stringSerde));

        new KafkaStreams(streamsBuilder.build(), buildConfig())
                .start();
        System.out.println("Started Kafka Streams example");
    }
}