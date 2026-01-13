package hr.fer.dippro;

import hr.fer.dippro.calc.VehicleSpeedCalculator;
import hr.fer.dippro.device.DeviceData;
import hr.fer.dippro.device.DeviceStatus;
import hr.fer.dippro.device.DeviceStatusUpdater;
import hr.fer.dippro.mqtt.MqttDetectionReceiver;
import hr.fer.dippro.mqtt.MqttStatusReceiver;
import hr.fer.dippro.mqtt.TopicData;
import hr.fer.dippro.server.DataServer;
import io.javalin.Javalin;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
//    private static final String brokerURL = "tcp://localhost:1884";
    private static final String brokerURL = "tcp://broker:1884";
    // "tcp://Mac-mini-od-Josip.local:1884"
    private static final String deviceDetection1 = "ESP32_154130/det";
    private static final String deviceDetection2 = "ESP32_157A00/det";

    private static final int dataServerStartRow = 5001;

    public static void main(String[] args) {
        BlockingQueue<Long> queueTimestamp1 = new LinkedBlockingQueue<>();
        BlockingQueue<Long> queueTimestamp2 = new LinkedBlockingQueue<>();

        BlockingQueue<Double> queueDeviceSpeed = new ArrayBlockingQueue<>(100);

        MqttDetectionReceiver detectionTopic1 = new MqttDetectionReceiver(
                brokerURL,
                "speed-app-client-det1",
                deviceDetection1,
                queueTimestamp1);
        MqttDetectionReceiver detectionTopic2 = new MqttDetectionReceiver(
                brokerURL,
                "speed-app-client-det2",
                deviceDetection2,
                queueTimestamp2);

        Thread t_detection1 = new Thread(detectionTopic1);
        Thread t_detection2 = new Thread(detectionTopic2);

        t_detection1.start();
        t_detection2.start();

        boolean speedCalcRunning = false;

        VehicleSpeedCalculator vehicleSpeedCalculator = new VehicleSpeedCalculator(
                queueTimestamp1,
                queueTimestamp2,
                20,
                1,
                50.,
                queueDeviceSpeed
        );

        Thread vehicleSpeedThread = new Thread(vehicleSpeedCalculator);
        vehicleSpeedThread.start();

        DataServer dataServer = new DataServer(dataServerStartRow);
        Thread dataServerThread = new Thread(dataServer);
        dataServerThread.start();

        while(true) {
            speedCalcRunning = true;

            try {
                while(queueDeviceSpeed.peek() != null){
                    System.out.println("Speed: " + queueDeviceSpeed.poll() + " m/s");
                }
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}