## RxMQTT

An [RxJava] wrapper for [MQTT]

Latest version: `0.0.2`

## Usage

Use the builder syntax to create a client and connect to the broker:

```java
    RxMqttClient client = new RxMqttClientBuilder()
                .setHost("test.mosquitto.org")
                .buildAndConnect();
```

Subscribe to a topic to recieve an RxJava Observable to recieve updates from this topic:

```java
client.onTopic("home/livingroom/temperatures")
      .subscribe(message -> {
          System.out.println("Topic: " + message.getTopic());
          System.out.println("Message: " + message.getMessage());
       });
```

To monitor the connection status:

```java
client.status()
      .subscribe(status -> System.out.println(status.isConnected()));
```

To publish a message on a topic:

```java
client.publish("home/livingroom/temperatures", "27 C");
      .subscribe(response -> System.out.println("Delivered: " + response));
```

You can also publish an object, which gets serialized to JSON

```java
Temperature temp = new Temperature(27, CELCIUS);
client.publish("home/livingroom/temperatures", temp);
      .subscribe(response -> System.out.println("Delivered: " + response));
```

Disconnect when done:

```java
client.disconnect();
```


## Download

```gradle
compile 'com.eliasbagley:rxmqtt:0.0.2'
```


## LICENSE

```
The MIT License

Copyright (c) Elias Bagley

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```

[RxJava]:https://github.com/Netflix/RxJava
[MQTT]:http://git.eclipse.org/c/paho/org.eclipse.paho.mqtt.java.git/
