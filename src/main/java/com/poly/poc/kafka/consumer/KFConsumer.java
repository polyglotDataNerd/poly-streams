package com.poly.poc.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.*;

public class KFConsumer {

    private Properties props;
    private List<ConsumerRecord<String, String>> syncinput = Collections.synchronizedList(new ArrayList<>());
    private KFProcessor processor = new KFProcessor(syncinput);
    public KFConsumer(Properties props) {
        this.props = props;
    }

    public void consume() {
        props.setProperty("boostrap.servers", "localhost:9092");
        props.setProperty("group.id", "cn_poc");
        props.setProperty("enable.auto.commit", "false");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        /* subscribe method subscribes to Kafka topic name */
        consumer.subscribe(Arrays.asList("test"));

        /* Consumers will be a single dedicated thread tied to partition(broker) within the topic. Processors wil be
        multi-threaded. Consumers will poll within a thread and output the processing to the processor which will use many threads */
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
        processor.process(records);
    }

}
