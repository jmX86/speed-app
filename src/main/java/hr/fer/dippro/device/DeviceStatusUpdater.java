package hr.fer.dippro.device;

import hr.fer.dippro.mqtt.TopicData;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class DeviceStatusUpdater implements Runnable{
    private final BlockingQueue<TopicData> inQueue;
    private final BlockingQueue<DeviceData> outQueue;
    private final Map<String, DeviceStatus> deviceStatus;
    private final Map<String, DeviceData> deviceDataCache;

    private boolean running = false;

    public DeviceStatusUpdater(
            BlockingQueue<TopicData> inQueue,
            Map<String, DeviceStatus> devicesStatus,
            BlockingQueue<DeviceData> outQueue,
            Map<String, DeviceData> deviceDataCache) {
        this.inQueue = inQueue;
        this.deviceStatus = devicesStatus;
        this.outQueue = outQueue;
        this.deviceDataCache = deviceDataCache;
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

                if(deviceDataCache.containsKey(device.deviceID())){
                    DeviceData deviceRecord = deviceDataCache.get(device.deviceID());
                    System.out.println("Updating "+device.deviceID()+" status to "+device.payload());
                    deviceDataCache.put(
                            device.deviceID(),
                            new DeviceData(device.deviceID(), status, deviceRecord.detections())
                    );
                }else{
                    System.out.println("Adding device "+device.deviceID()+" with status "+device.payload());
                    deviceDataCache.put(
                            device.deviceID(),
                            new DeviceData(device.deviceID(), status, new ArrayBlockingQueue<>(100))
                    );
                }

                deviceStatus.put(device.deviceID(), status);

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
