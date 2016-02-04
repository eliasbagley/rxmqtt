## RxMQTT

An RxJava wrapper for MQTT

## Usage

Use the builder syntax to create a client

```java
    RxMqttClient client = new RxMqttClientBuilder()
                .setClientId("my-client")
                .setCleanSession(true)
                .setKeepAliveInterval(1000)
                .setHost("test.mosquitto.org")
                .setPort("1883")
                .setWill(new Will("my/topic", "I died!"))
                .build();
```

Connect to the network


```java
client.connect();
```

Subscribe to a topic to recieve an RxJava Observable to recieve updates from this topic

```java
client.topic("home/livingroom/temperatures")
      .subscribe(message -> {
          System.out.println(String.format("Received message %s from topic %s", message.getMessage(), message.getTopic()));
       });
```


Disconnect when done

```java
client.disconnect();
```


To start using, add the following line to `build.gradle`:

`compile 'com.eliasbagley:rxmqtt:0.0.1'`


## License

MIT License
