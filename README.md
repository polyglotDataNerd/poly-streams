# poly-streams

This project uses a multithreaded/parallel processing framework to listen and process event level data from a data stream and transform events to be put into a Data Lake. Specifically the stream is only ingesting events used in an Apache Kafka producer. It's generic enough to apply to any Kafka event-stream process. 


Dependencies:

* Java 8 
* [Kafka 2.5](https://kafka.apache.org/22/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html)
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
     
 1. [KFConsumer](src/main/java/com/poly/poc/kafka/KFConsumer.java): This object is the entry class to run the pipeline it uses the [ExecutorService](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html) interface to maintain thread health. This entry point creates the worker and the worker factories that poll and process from the stream.
 2. [KFProcessor](src/main/java/com/poly/poc/kafka/KFProcessor.java): This object is the processor, it has an internal queue to manage the processing and the polling of data to not maximize resources. 

**Application Arguments:**

| Argument        | Sample           | Required  |
| ------------- |:-------------:| -----:|
| environment     | us-west-2-prod | YES: CI pipeline build |

When the java app is compiled and built the sample call to run the application in Fargate with arguments would look something like this
            
    java -jar poly-streams-1.0-ecs.jar environment
        
        
Infrastructure
-          
    
Terraform Modules:

* [infra](https://github.com/polyglotDataNerd/poly-streams/tree/master/infrastructure/infra): This builds the current ECS and Cloudwatch infrastructure to house container service.   
 
* [app](https://github.com/polyglotDataNerd/poly-streams/tree/master/infrastructure/app): This builds the container service and references the docker image in ECR and also the ECS cluster the services will run in. 

Build:

* [apply](https://github.com/polyglotDataNerd/poly-streams/blob/master/infrastructure/apply.sh): This shell takes an environment variable and builds the end to end service. 
    
        source ~/poly-streams/infrastructure/apply.sh "us-west-2-prod"
 
* [destroy](https://github.com/polyglotDataNerd/poly-streams/blob/master/infrastructure/destroy.sh): This This shell takes an environment variable and destroys the end to end service.
    
        source ~/poly-streams/infrastructure/destroy.sh "us-west-2-prod" 