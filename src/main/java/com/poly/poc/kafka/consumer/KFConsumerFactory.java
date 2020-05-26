package com.poly.poc.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class KFConsumerFactory {

    private KafkaConsumer<String, String> consumer;
    public KFConsumerFactory(KafkaConsumer<String, String> consumer) {
        this.consumer = consumer;
    }

    private ConcurrentLinkedDeque<ConsumerRecords<String, String>> consumerQueue = new ConcurrentLinkedDeque<>();
    private KFProcessor processor = new KFProcessor(consumerQueue);


    public void consume() {
        /* Consumers will be a single dedicated thread tied to partition(broker) within the topic. Processors wil be
        multi-threaded. Consumers will poll within a thread and output the processing to the processor which will use many threads */
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
        processor.process(records);
    }

}
