# Spring Boot Micro-Services

## Guitar Store - Gateway service

### Startup

`mvn spring:boot:run`

### Shutdown

`CTRL-C`

### Configuration

#### Environment Variables

* INSTANCE_ID: The instance ID to append to reply topic names for request/response events (default: a random UUID).
* KAFKA_HOST: The hostname of the Kafka instance to connect to (default: localhost).
* KAFKA_PORT: The port that the Kafka instance is listening on (default: 9092).
* KAFKA_TOPIC_AMPS_PROVIDED: The name of the Kafka topic for the "amps provided" event
  (default: sbms.event.amps-provided).
* KAFKA_TOPIC_GUITARS_PROVIDED: The name of the Kafka topic for the "guitars provided" event
  (default: sbms.event.guitars-provided).
* KAFKA_TOPIC_AMPS_REQUESTED: The name of the Kafka topic for the "amps requested" event
  (default: sbms.event.amps-requested).
* KAFKA_TOPIC_AMPS_REQUESTED_TIMEOUT: The timeout in milliseconds of the Kafka topic for the "amps requested" event
  (default: 2000);
* KAFKA_TOPIC_GUITARS_REQUESTED: The name of the Kafka topic for the "guitars requested" event
  (default: sbms.event.guitars-requested).
* KAFKA_TOPIC_GUITARS_REQUESTED_TIMEOUT: The timeout in milliseconds of the Kafka topic for the "guitars requested"
  event (default: 2000);
