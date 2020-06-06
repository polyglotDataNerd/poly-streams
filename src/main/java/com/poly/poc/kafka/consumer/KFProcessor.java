package com.poly.poc.kafka.consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class KFProcessor {
    private int threads = 5;
    private ExecutorService threadPool = Executors.newFixedThreadPool(threads);
    private CountDownLatch latch = new CountDownLatch(threads);
    private KafkaConsumer<String, String> consumer;
    private ConcurrentLinkedDeque<ConsumerRecords<String, String>> consumerQueue;
    private final Log LOG = LogFactory.getLog(KFProcessor.class);

    public KFProcessor(KafkaConsumer<String, String> consumer, ConcurrentLinkedDeque<ConsumerRecords<String, String>> consumerQueue) {
        this.consumer = consumer;
        this.consumerQueue = consumerQueue;
        /* initializes all worker threads */
        IntStream.range(0, threads).parallel().forEach(x -> {
                    threadPool.submit(() -> {
                        try {
                            new Workers().run();
                        } catch (Exception e) {
                            Thread.currentThread().interrupt();
                            LOG.error(e.getStackTrace() + "->" + e.getMessage());
                        } catch (Throwable t) {
                            LOG.error("Shutting Down Processor");
                            System.exit(1);
                        } finally {
                            latch.countDown();
                        }
                    });

                }
        );
    }

    public void process(ConsumerRecords<String, String> records) {
        if (records.count() != 0) {
            consumerQueue.add(records);
            /* finer control of partition offsets */
            checkpoint(records);
        }
    }

    private class Workers implements Runnable {
        /*Flag to shutdown workers.*/
        volatile boolean shutdown = false;

        @Override
        public void run() {
            while (!shutdown) {
                try {
                    Thread.sleep(5000);
                    processors();
                } catch (Exception e) {
                    //LOG.warn(String.format("%s%b%s%s", "Processor Interruption Shutdown: ", shutdown, "\t", e));
                }
            }
        }

        private void processors() {
            ConsumerRecords<String, String> records = consumerQueue.poll();
            records
                    .forEach(record -> {
                        Map<String, String> payload = Collections.singletonMap(record.partition() + ":" + record.offset(), record.partition() + ":" + record.offset() + "-> " + record.value());
                        LOG.info(payload.values().stream().map(Object::toString).collect(Collectors.joining(",")));
                    });
        }
    }

    private synchronized void checkpoint(ConsumerRecords<String, String> r) {
        for (TopicPartition partition : r.partitions()) {
            List<ConsumerRecord<String, String>> partitionRecords = r.records(partition);
            long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
            long currentOffset = lastOffset + 1;
            //LOG.info("checkpoint partition offset: " + partition + " -> " + currentOffset);
            consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(currentOffset)));
        }
    }
}
