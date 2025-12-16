package hr.fer.dippro.device;

import hr.fer.dippro.mqtt.TopicData;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class DeviceStatusUpdater implements Runnable{
    private final BlockingQueue<TopicData> inQueue;
    private final BlockingQueue<DeviceData> outQueue;
    private final Map<String, DeviceStatus> deviceStatus;
    private final Map<String, BlockingQueue<Long>> deviceQueueCache;

    private boolean running = false;

    public DeviceStatusUpdater(
            BlockingQueue<TopicData> inQueue,
            Map<String, DeviceStatus> devicesStatus,
            BlockingQueue<DeviceData> outQueue,
            Map<String, BlockingQueue<Long>> deviceQueueCache) {
        this.inQueue = inQueue;
        this.deviceStatus = devicesStatus;
        this.outQueue = outQueue;
        this.deviceQueueCache = deviceQueueCache;
    }

    @Override
    public void run() {
        running = true;

        while(running) {
            try {
                TopicData device = inQueue.poll(5000, TimeUnit.MILLISECONDS);
                if(device == null) {
                    continue;
                }
                if(deviceStatus.containsKey(device.deviceID())){
                    System.out.println("Updating "+device.deviceID()+" status to "+device.payload());
                }else{
                    System.out.println("Adding device "+device.deviceID()+" with status "+device.payload());
                }
                DeviceStatus status;
                if(device.payload()!=null){
                    if(device.payload().equals("START")){
                        status = DeviceStatus.START;
                    }else {
                        status = DeviceStatus.STOP;
                    }
                }else{
                    status = DeviceStatus.STOP;
                }
                deviceStatus.put(device.deviceID(), status);

                deviceQueueCache.putIfAbsent(device.deviceID(), new LinkedBlockingQueue<>(100));

                outQueue.offer(new DeviceData(device.deviceID(), status, deviceQueueCache.get(device.deviceID())));

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        running = false;
    }

    public void stop(){
        running = false;
    }
}
