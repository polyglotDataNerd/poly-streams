package com.poly.poc.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KFProcessor {

    ExecutorService pool = Executors.newFixedThreadPool(5);
    private List<ConsumerRecord<String, String>> syncedCollection;

    public KFProcessor(List<ConsumerRecord<String, String>> syncedCollection) {
        this.syncedCollection = Collections.synchronizedList(syncedCollection);
    }

    public void process(ConsumerRecords<String, String> records) {

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
