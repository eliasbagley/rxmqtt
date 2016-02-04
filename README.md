## RxMQTT

An RxJava wrapper for MQTT

Latest version: 0.0.1

## Usage

Use the builder syntax to create a client

```java
    RxMqttClient client = new RxMqttClientBuilder()
                .setClientId("my-client")
                .setHost("test.mosquitto.org")
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
          System.out.println("Topic: " + message.getTopic());
          System.out.println("Message: " + message.getMessage());
       });
```


Disconnect when done

```java
client.disconnect();
```


To start using, add the following line to `build.gradle`:

```gradle
compile 'com.eliasbagley:rxmqtt:0.0.1'
```


## LICENSE

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
