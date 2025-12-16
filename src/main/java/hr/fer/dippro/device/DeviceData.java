package hr.fer.dippro.device;

import java.util.concurrent.BlockingQueue;

public record DeviceData(String deviceID, DeviceStatus status, BlockingQueue<Long> detections) {
}
