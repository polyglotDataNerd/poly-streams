# poly-streams

This project uses a multithreaded/parallel processing framework to listen and process event level data from a data stream and transform events to be put into a Data Lake. Specifically the stream is only ingesting events used in an Apache Kafka producer. It's generic enough to apply to any Kafka event-stream process. 


Dependencies:

* Java 8 
* [Kafka 2.5.0](https://kafka.apache.org/downloads#2.5.0)
* s3
* [Maven](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html)
* [Kafka Docker Images](https://hub.docker.com/r/confluentinc/cp-kafka/)

Consumer
-   
* Since it's using maven as the build tool you need to install a local repo on machine in order
to generate dependant libraries within the pom.xml file. 

        Follow this tutorial to setup quickly:
        install: 
         1. manual: https://maven.apache.org/install.html
         2. homebrew (preferred): http://brewformulas.org/Maven
        quick guide: https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html

        MAVEN Project to rebuild run:
        1. mvn clean
        2. mvn package
        3. will compile and generate package (.jar) 
 

Notable Classes:   
     
 1. [KFConsumerFactory](./src/main/java/com/poly/poc/kafka/consumer/KFConsumerFactory.java): Factory class that will run it's own processor. 
 2. [KFProcessor](./src/main/java/com/poly/poc/kafka/consumer/KFProcessor.java): This object is the processor. The KafkaConsumer instance is meant to be single threaded per partition; this processor takes the records within the partition and processes it via an internal dequeue in a multi-threaded pattern. The threads are limited to the ExecutorService which manages the thread pool. 

**Application Arguments: TODO**

| Argument        | Sample           | Required  |
| ------------- |:-------------:| -----:|
| None     |  |  |


Infrastructure
-          
    
Docker:

* [Infrastructure](./infrastructure/single-node-kafka): To run Kafka 2.5.0 locally to test the consumer you can run docker-compose which will build and run Zookeeper along with Kafka. 

   - [Run Docker Build](./infrastructure/CreateTopic.sh) 
 
