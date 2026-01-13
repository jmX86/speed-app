package hr.fer.dippro.mqtt;

import hr.fer.dippro.device.DeviceData;
import org.eclipse.paho.client.mqttv3.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class MqttDetectionReceiver implements Runnable {
    private final String brokerURL;
    private final String clientID;
    private final String topic;
    private MqttClient client;

    private boolean running = false;

    private final BlockingQueue<Long> deviceDetections;

    public MqttDetectionReceiver(
            String brokerURL,
            String clientID,
            String topic,
            BlockingQueue<Long> deviceDetections) {
        this.brokerURL = brokerURL;
        this.clientID = clientID;
        this.topic = topic;
        this.deviceDetections = deviceDetections;
    }

    @Override
    public void run() {
        running = true;

        try{
            client = new MqttClient(brokerURL, clientID);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    System.out.println("Connection to "+brokerURL+" lost. Cause "+throwable.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                    long timestamp = System.currentTimeMillis();
                    String deviceID;

                    System.out.println("<"+timestamp+"> "+"Message arrived: ["+topic+"]: "+new String(mqttMessage.getPayload()));
                    if(topic.contains("/")){
                        String[] topicSplit = topic.split("/");
                        if(topicSplit.length == 2){
                            deviceID = topicSplit[0];
                        }else {
                            return;
                        }
                    }else{
                        return;
                    }

                    deviceDetections.offer(timestamp);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}
            });

            client.connect();
            client.subscribe(topic);

            while (running) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    System.out.println("Thread interrupted: "+e.getMessage());
                    running = false;
                }
            }
        } catch (MqttException e) {
            System.out.println("MQTT Exception: "+e.getMessage());
        }
    }

    public void stop() {
        running = false;
    }

}
