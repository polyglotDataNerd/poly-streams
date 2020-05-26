package com.poly.poc.kafka.consumer;

import com.poly.poc.utils.Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class KFProcessor {
    private int threads = 5;
    private ExecutorService threadPool = Executors.newFixedThreadPool(threads);
    private CountDownLatch latch = new CountDownLatch(threads);
    private KafkaConsumer<String, String> consumer;
    private final ConcurrentLinkedDeque<ConsumerRecords<String, String>> consumerQueue;
    private static final Log LOG = LogFactory.getLog(KFProcessor.class);

    public KFProcessor(KafkaConsumer<String, String> consumer, ConcurrentLinkedDeque<ConsumerRecords<String, String>> consumerQueue) {
        this.consumer = consumer;
        this.consumerQueue = consumerQueue;
        /* initializes all worker threads */
        IntStream.range(1, threads).forEach(x -> {
                    threadPool.submit(() -> {
                        try {
                            new Workers().run();
                        } catch (Exception e) {
                            Thread.currentThread().interrupt();
                            LOG.error(e.getStackTrace() + "->" + e.getMessage());
                        } catch (Throwable t) {
                            LOG.error("Shutting Down Processor");
                            System.exit(1);
                        }
                            /*finally {
                                latch.countDown();
                            }*/
                    });

                }
        );
    }

    public void process(ConsumerRecords<String, String> records) {
        consumerQueue.add(records);
    }

    private class Workers implements Runnable {

        /*Flag to shutdown workers.*/
        volatile boolean shutdown = false;
        @Override
        public void run() {
            while (!shutdown) {
                try {
                    processors();
                } catch (Exception e) {
                    shutdown = true;
                    LOG.error("Processor Interruption Shutdown: " + shutdown);
                    break;
                }
            }
        }

        private void processors() {
            consumerQueue
                    .poll()
                    .forEach(record -> {
                        new Transformer(record.value())
                                .transform()
                                .entrySet()
                                .parallelStream()
                                .forEach(k -> {
                                    System.out.println(k.getKey() + ":" + k.getValue());
                                });
                    });
            /* Manual Offset Control */
            consumer.commitSync();

        }

    }
}
