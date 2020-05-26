package com.poly.poc.kafka.consumer;

import com.sun.corba.se.spi.orbutil.threadpool.Work;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KFProcessor {

    private ExecutorService pool = Executors.newFixedThreadPool(5);
    private List<ConsumerRecord<String, String>> syncedCollection;
    private static final Log LOG = LogFactory.getLog(KFProcessor.class);

    public KFProcessor(List<ConsumerRecord<String, String>> syncedCollection) {
        this.syncedCollection = Collections.synchronizedList(syncedCollection);
    }

    public void process(ConsumerRecords<String, String> records) {
        Workers runnable = new Workers();
        pool.submit(() -> {
            try {
                while (!pool.isShutdown()) {
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

        }

    }
}
