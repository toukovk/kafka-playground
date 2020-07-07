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