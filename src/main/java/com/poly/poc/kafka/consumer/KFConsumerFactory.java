package com.poly.poc.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.concurrent.ConcurrentLinkedDeque;

public class KFConsumerFactory {

    private KafkaConsumer<String, String> consumer;
    private ConcurrentLinkedDeque<ConsumerRecords<String, String>> consumerQueue;
    private KFProcessor processor;


    public KFConsumerFactory(KafkaConsumer<String, String> consumer, ConcurrentLinkedDeque<ConsumerRecords<String, String>> consumerQueue, KFProcessor processor) {
        this.consumer = consumer;
        this.consumerQueue = consumerQueue;
        this.processor = processor;
    }

    public void consume() {
        /* Consumers will be a single dedicated thread tied to partition(broker) within the topic. Processors wil be
        multi-threaded. Consumers will poll within a thread and output the processing to the processor which will use many threads */
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
        processor.process(records);
    }

}
