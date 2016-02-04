package com.eliasbagley.rxmqtt;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.eliasbagley.rxmqtt.impl.Message;
import com.eliasbagley.rxmqtt.impl.RxMqttClient;
import com.eliasbagley.rxmqtt.impl.RxMqttClientBuilder;
import com.eliasbagley.rxmqtt.impl.RxMqttClientStatus;
import com.eliasbagley.rxmqtt.impl.Will;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

    RxMqttClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupClient();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.disconnect();
    }


    private void setupClient() {
        Toast.makeText(MainActivity.this, "Setting up MQTT client...", Toast.LENGTH_SHORT).show();

        client = new RxMqttClientBuilder()
                .setClientId("my-client")
                .setHost("test.mosquitto.org")
                .build();

        client.connect()
                .subscribe(new Action1<IMqttToken>() {
                    @Override
                    public void call(IMqttToken token) {
                        Toast.makeText(MainActivity.this, token.toString(), Toast.LENGTH_SHORT).show();

                    }
                });

        client.statusReport()
                .subscribe(new Action1<RxMqttClientStatus>() {
                    @Override
                    public void call(RxMqttClientStatus status) {
                        Toast.makeText(MainActivity.this, status.toString(), Toast.LENGTH_SHORT).show();

                    }
                });

        client.subscribeTopic("my_topic", 1);

        client.topic("my_topic", 1)
                .subscribe(new Action1<Message>() {
                    @Override
                    public void call(Message message) {
                        Toast.makeText(MainActivity.this, message.toString(), Toast.LENGTH_LONG).show();
                    }
                });

//        // TODO: I think a better API would be to subscribe to a complete/incomplete notification
//        client.publish("room/derp/topic", myObject)
//                .qos(1)
//                .retain()
//                .subscribe(new Action1<PublishResult>() {
//                    @Override
//                    public void call(PublishResult result) {
//                        Message message = result.getMessage();
//
//                    }
//                });
    }

    /*

    extensions:
    add qos as a stream operator
    add the varargs to sub to multiple topics at once

    brainstorming a Retrofit-Like API..

    public interface TemperatureStreams {
        @Subscribe("rooms/{room_name}/temperature", 1)
        Observable<Message> subRoomTemp(@Path("room_name") String name);

        @Publish("rooms/{room_name}/temperature", 1)
        Observable<Result> pubTemp(@Path("room_name") String name, @Body Temperature);
    }

    ... later

    Client client = new ClientBuilder().build();

    TemperatureStreams temp = client.create(TemperatureStreams.class);

    temp.subRoomTemp("derp")
        .subscribe(...)



     */

}
