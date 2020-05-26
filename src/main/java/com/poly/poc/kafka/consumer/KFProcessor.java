package com.poly.poc.kafka.consumer;

import com.poly.poc.utils.Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KFProcessor {

    private ExecutorService threadPool = Executors.newFixedThreadPool(5);
    private final ConcurrentLinkedDeque<ConsumerRecords<String, String>> consumerQueue;
    private static final Log LOG = LogFactory.getLog(KFProcessor.class);

    public KFProcessor(ConcurrentLinkedDeque<ConsumerRecords<String, String>> consumerQueue) {
        this.consumerQueue = consumerQueue;
    }

    public void process(ConsumerRecords<String, String> records) {
        Workers runnable = new Workers();
        threadPool.submit(() -> {
            try {
                while (!threadPool.isShutdown()) {
                    consumerQueue.add(records);
                    runnable.run();
                }
            } catch (Exception e) {
                LOG.error(e.getStackTrace() + "->" + e.getMessage());
            } catch (Throwable t) {
                LOG.error("Shutting Down Processor");
                System.exit(1);
            }
        });
    }

    private class Workers implements Runnable {

        /*Flag to shutdown the queue consumer.*/
        volatile boolean shutdown = false;

        @Override
        public void run() {
            while (!shutdown) {
                if (shutdown = true) {
                    break;
                }
                try {
                    processors();
                } catch (Exception e) {
                    shutdown = true;
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
        }

    }
}
