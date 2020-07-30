A Docker Compose setup with some examples for easily trying out locally various Kafka related services, in order to get hands-on feeling on the tools & APIs.

NOTE that the setup & configuration is not suitable for actual usage.

If you're interested in event-driven architectures & Kafka, I recommend reading [Designing Event-Driven Systems (Ben Stepford)](http://www.benstopford.com/2018/04/27/book-designing-event-driven-systems/) (ebook freely available)

## Start the environment

```
docker-compose up -d
```

## Kafka CLI tools

Reference: https://docs.confluent.io/current/installation/cli-reference.html

Some examples of using Kafka command-line tools

```
# Create a topic
docker-compose exec kafka kafka-topics --create --topic my-topic --partitions 1 --replication-factor 1 --if-not-exists --zookeeper zookeeper:2181

# List topics
docker-compose exec kafka kafka-topics --list --zookeeper zookeeper:2181
# Describe a topic
docker-compose exec kafka kafka-topics --describe --topic my-topic --zookeeper zookeeper:2181

# Produce to a topic
docker-compose exec kafka bash -c "echo 'test message' | kafka-console-producer --request-required-acks 1 --broker-list kafka:9092 --topic my-topic"

# Consume a topic
docker-compose exec kafka kafka-console-consumer --bootstrap-server kafka:9092 --topic my-topic --from-beginning --max-messages 1
```

## Kafka Control Center

Available at http://localhost:9021

## REST Proxy examples

Full API at https://docs.confluent.io/current/kafka-rest/api.html

### Produce messages to a topic

```
curl -XPOST \
     -H "Content-Type: application/vnd.kafka.json.v2+json" \
     -H "Accept: application/vnd.kafka.v2+json" \
     --data '{"records":[{"value":{"field1":"value1"}},{"value":{"field1":"value2"}},{"value":{"field1":"value3"}}]}' \
     "http://localhost:8082/topics/my-topic-for-rest" | jq "."
```

### Metadata

```
# Get topics
curl "http://localhost:8082/topics" | jq "."
# Get info on a topic "my-topic-for-rest":
curl "http://localhost:8082/topics/my-topic-for-rest" | jq "."
```

### Consume messages

REST Proxy works with consumer instances tied to a REST Proxy instance

```
# Create a consumer instance "my-consumer-instance" under consumer group "my-consumer-group"
curl -XPOST \
     -H "Content-Type: application/vnd.kafka.v2+json" \
     --data '{"name": "my-consumer-instance", "format": "json", "auto.offset.reset": "earliest"}' \
     http://localhost:8082/consumers/my-consumer-group | jq "."

# Subscribe to topic "my-topic-for-rest" with "my-consumer-instance"
curl -XPOST \
     -H "Content-Type: application/vnd.kafka.v2+json" \
     --data '{"topics":["my-topic-for-rest"]}' \
     http://localhost:8082/consumers/my-consumer-group/instances/my-consumer-instance/subscription

# Consume messages with the consumer (after the subscription done above)
curl -H "Accept: application/vnd.kafka.json.v2+json" \
     http://localhost:8082/consumers/my-consumer-group/instances/my-consumer-instance/records | jq "."
```

## KSQLDB examples

Start KSQLDB CLI

```
docker-compose exec ksqldb-cli ksql http://ksqldb-server:8088
```

Read metadata:

```
ksql> SHOW STREAMS;
ksql> SHOW TOPICS;
```

Create a stream (backed by a new Kafka topic `myevents`)

```
ksql> CREATE STREAM myEvents (userId VARCHAR, type INTEGER)
  WITH (kafka_topic='myevents', value_format='json', partitions=1);
```

Run a continuous query in CLI

```
ksql> SELECT * FROM myEvents 
  EMIT CHANGES;
```

Write messages to a stream:

```
ksql> INSERT INTO myEvents (userId, type) VALUES ('user1', 1);
ksql> INSERT INTO myEvents (userId, type) VALUES ('user1', 4);
ksql> INSERT INTO myEvents (userId, type) VALUES ('user2', 2);
ksql> INSERT INTO myEvents (userId, type) VALUES ('user2', 5);
```

### Creating derived streams (transformations)

First, configure ksqldb to consume topic from the beginning:

```
ksql> SET 'auto.offset.reset' = 'earliest';
```

Create a derived stream (stateless filtering, creates also a Kafka topic):

```
ksql> CREATE STREAM myOddTypeEvents AS
  SELECT userId, type
  FROM myEvents
  WHERE type % 2 = 1
  EMIT CHANGES;
```

Create a stateful stream

```
ksql> CREATE TABLE eventCountByType AS
  SELECT type, COUNT(*) AS count
  FROM myEvents
  GROUP BY type
  EMIT CHANGES;
```

### Further KSQLDB links:

* Examples: https://ksqldb.io/examples.html
* Developer guide: https://docs.ksqldb.io/en/latest/developer-guide/

## TODOs

* Kafka Connect
* Kafka Streams
* Some example setup
