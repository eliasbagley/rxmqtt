package com.eliasbagley.rxmqtt;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

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

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Clicked floating action button");
                client.publish("my_topic", "hello world from app!").subscribe(new Action1<PublishResponse>() {
                    @Override
                    public void call(PublishResponse publishResponse) {
                        System.out.println("IN callback");
                    }
                });
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.disconnect();
    }

    private void setupClient() {
        client = new RxMqttClientBuilder()
                .setHost("test.mosquitto.org")
                .buildAndConnect();

//        client.connect();
//                .flatMap(new Func1<RxMqttClient, Observable<IMqttToken>>() {
//                    @Override
//                    public Observable<IMqttToken> call(RxMqttClient rxMqttClient) {
//                        System.out.println("Inside the flatmap");
//                        return rxMqttClient.subscribeTopic("my_topic", 1);
//                    }
//                })
//                .subscribe(new Action1<IMqttToken>() {
//                    @Override
//                    public void call(IMqttToken iMqttToken) {
//                        System.out.println("Finished subscribe to topic..");
//                    }
//                });


        //TODO must abstract away this CONNECTED filter and the client connection
        // TODO perhaps have this state filter inside of the client itself?

        client.onTopic("my_topic")
                .subscribe(new Action1<Message>() {
                    @Override
                    public void call(Message message) {
                        System.out.println("Received message: " + message.toString());
                    }
                });


        client.publish("my_topic", "hello world from app! der pp").subscribe(new Action1<PublishResponse>() {
            @Override
            public void call(PublishResponse publishResponse) {
                System.out.println("IN callback");
            }
        });

//        client.status().filter(new Func1<Status, Boolean>() {
//            @Override
//            public Boolean call(Status status) {
//                return status.getState() == State.CONNECTED;
//            }
//        }).flatMap(new Func1<Status, Observable<Message>>() {
//            @Override
//            public Observable<Message> call(Status status) {
//                System.out.println("Inside the flatmap");
//                return client.subscribeTopic("my_topic", 1);
//            }
//        })
//        .subscribe(new Action1<Message>() {
//            @Override
//            public void call(Message message) {
//                System.out.println(String.format("Got message: %s", message.toString()));
//            }
//        });

        client.status()
                .subscribe(new Action1<Status>() {
                    @Override
                    public void call(Status status) {
                        System.out.println(String.format("Status updated: %s", status.toString()));
                    }
                });

//        client.topic("my_topic", 1)
//                .subscribe(new Action1<Message>() {
//                    @Override
//                    public void call(Message message) {
//                        System.out.println(String.format("Got message: %s", message.toString()));
//                        Toast.makeText(MainActivity.this, message.toString(), Toast.LENGTH_LONG).show();
//                    }
//                });

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
