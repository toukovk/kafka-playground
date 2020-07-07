Start

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