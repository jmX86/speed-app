package hr.fer.dippro.calc;

import hr.fer.dippro.device.DeviceData;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class VehicleSpeedCalculator implements Runnable {
    private final BlockingQueue<Long> firstSensorTimestampQueue;
    private final BlockingQueue<Long> secondSensorTimestampQueue;
    private final double distance;    // m
    private final BlockingQueue<Double> speedQueue;

    private final Double maxSpeed;  // m/s
    private final Double minSpeed;

    private final Long detectionTimeout;// s

    public VehicleSpeedCalculator(
            BlockingQueue<Long> firstSensorTimestampQueue,
            BlockingQueue<Long> secondSensorTimestampQueue,
            double distance,
            double minSpeed,
            double maxSpeed,
            BlockingQueue<Double> speedQueue) {
        this.firstSensorTimestampQueue = firstSensorTimestampQueue;
        this.secondSensorTimestampQueue = secondSensorTimestampQueue;
        this.distance = distance;   // meters
        this.speedQueue = speedQueue;
        this.maxSpeed = maxSpeed;   // m/s
        this.minSpeed = minSpeed;   // m/s

        this.detectionTimeout = (long) (distance / minSpeed); // seconds

        System.out.println("VehicleSpeedCalculator constructor: dist=" + distance + ", minSpeed=" + minSpeed + ", maxSpeed=" + maxSpeed +  ", detectionTimeout=" + detectionTimeout);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Long firstTimestamp = firstSensorTimestampQueue.poll(detectionTimeout, TimeUnit.SECONDS);
                Long secondTimestamp = secondSensorTimestampQueue.poll(detectionTimeout, TimeUnit.SECONDS);

                if(firstTimestamp == null && secondTimestamp == null) {
                    continue;
                }else if (firstTimestamp != null && secondTimestamp == null) {
                    if(System.currentTimeMillis() - firstTimestamp > detectionTimeout*1000) {
                        // If first is detected and second is not for more than timeout.
                        // Second is probably missed so remove first.
                        System.out.println("Second detection missing");
                        continue;
                    }
                    continue;
                } else if (firstTimestamp == null && secondTimestamp != null) {
                    if(System.currentTimeMillis() - secondTimestamp > 2000) {
                        // If second is detected before first plus 2s of waiting delay. First is missed so remove second.
                        System.out.println("First detection missing");
                        continue;
                    }
                    continue;
                } else {
                    long timeDelta = secondTimestamp - firstTimestamp;
                    if(timeDelta > detectionTimeout*1000) {
                        System.out.println("Timeout detected. " +  timeDelta + " ms");
                        continue;
                    }
                }

                if(secondTimestamp <= firstTimestamp ) {
                    System.out.println("Wrong detection order");
                    continue;
                }

                speedQueue.add(distance*1000/(secondTimestamp - firstTimestamp));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
