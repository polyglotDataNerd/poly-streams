package com.poly.poc.kafka.consumer;

import com.poly.poc.utils.ConfigProps;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.ConsumerConfig;
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
        Log LOG = LogFactory.getLog(Consumer.class);

        /* Kafka consumer properties */
        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getPropValues("servers"));
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, config.getPropValues("consumerGroup"));
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.getPropValues("offsetReset"));
        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, config.getPropValues("autoCommit"));
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        /* subscribe method subscribes to Kafka topic name */
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        ConcurrentLinkedDeque<ConsumerRecords<String, String>> consumerQueue = new ConcurrentLinkedDeque<>();
        consumer.subscribe(Arrays.asList(config.getPropValues("topics").split(",")));
        KFConsumerFactory consumerFactory = new KFConsumerFactory(consumer, consumerQueue, new KFProcessor(consumer, consumerQueue));

        /* run consumer */
        try {
            while (true) {
                consumerFactory.consume();
            }
        } catch (Exception e) {
            LOG.error(e);
        } finally {
            consumer.close();
        }
    }

}
