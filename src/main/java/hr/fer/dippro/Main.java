package hr.fer.dippro;

import hr.fer.dippro.device.DeviceData;
import hr.fer.dippro.device.DeviceStatus;
import hr.fer.dippro.device.DeviceStatusUpdater;
import hr.fer.dippro.mqtt.MqttDetectionReceiver;
import hr.fer.dippro.mqtt.MqttStatusReceiver;
import hr.fer.dippro.mqtt.TopicData;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        BlockingQueue<TopicData> queueDeviceStatusTopicData = new LinkedBlockingQueue<>(20);
        BlockingQueue<DeviceData> queueDeviceStatusUpdate = new LinkedBlockingQueue<>(20);

        Map<String, DeviceStatus> devicesStatus = new ConcurrentHashMap<>();
        Map<String, BlockingQueue<Long>> deviceQueueCache = new ConcurrentHashMap<>();

        MqttStatusReceiver statusTopic = new MqttStatusReceiver(
                "tcp://Mac-mini-od-Josip.local:1884",
                "speed-app-client-stat",
                "+/status",
                queueDeviceStatusTopicData);

        DeviceStatusUpdater deviceStatusUpdater = new DeviceStatusUpdater(
                queueDeviceStatusTopicData,
                devicesStatus,
                queueDeviceStatusUpdate,
                deviceQueueCache
        );

        Thread statusUpdateThread = new Thread(deviceStatusUpdater);

        statusUpdateThread.start();

        MqttDetectionReceiver detectionTopic = new MqttDetectionReceiver(
                "tcp://Mac-mini-od-Josip.local:1884",
                "speed-app-client-det",
                "+/det",
                queueDeviceStatusUpdate);

        Thread t_detection = new Thread(detectionTopic);
        Thread t_status = new Thread(statusTopic);

        t_detection.start();
        t_status.start();

        while(true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}