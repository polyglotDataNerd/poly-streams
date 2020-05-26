package com.poly.poc.kafka.consumer;

import com.poly.poc.kafka.consumer.KFConsumerFactory;
import com.poly.poc.utils.ConfigProps;
import org.apache.commons.io.FileUtils;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Consumer {

    private static ConfigProps config = new ConfigProps();

    public static void main(String... args) throws Exception {
        /*loads log files*/
        FileUtils.deleteQuietly(new File("/var/tmp/kafkaconsumer.log"));
        FileUtils.touch(new File("/var/tmp/kafkaconsumer.log"));
        System.setProperty("logfile.name", "/var/tmp/kafkaconsumer.log");
        config.loadLog4jprops();

        /* Kafka consumer properties */
        Properties props = new Properties();
        props.setProperty("boostrap.servers", config.getPropValues("servers"));
        props.setProperty("group.id", config.getPropValues("consumerGroup"));
        props.setProperty("auto.offset.reset", config.getPropValues("offsetReset"));
        props.setProperty("enable.auto.commit", config.getPropValues("autoCommit"));
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        /* subscribe method subscribes to Kafka topic name */
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        ConcurrentLinkedDeque<ConsumerRecords<String, String>> consumerQueue = new ConcurrentLinkedDeque<>();
        consumer.subscribe(Arrays.asList(config.getPropValues("topics").split(",")));
        KFConsumerFactory consumerFactory = new KFConsumerFactory(consumer, consumerQueue);

        /* run consumer */
        while (true) {
            consumerFactory.consume();
        }
    }

}
