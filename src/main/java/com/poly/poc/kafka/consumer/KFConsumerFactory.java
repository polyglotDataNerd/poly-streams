package com.poly.poc.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.*;

public class KFConsumerFactory {

    private List<ConsumerRecord<String, String>> syncinput = Collections.synchronizedList(new ArrayList<>());
    private KFProcessor processor = new KFProcessor(syncinput);
    private  KafkaConsumer<String, String> consumer;

    public KFConsumerFactory(KafkaConsumer<String, String> consumer) {
        this.consumer = consumer;
    }

    public void consume() {
        /* Consumers will be a single dedicated thread tied to partition(broker) within the topic. Processors wil be
        multi-threaded. Consumers will poll within a thread and output the processing to the processor which will use many threads */
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
        processor.process(records);
    }

}
