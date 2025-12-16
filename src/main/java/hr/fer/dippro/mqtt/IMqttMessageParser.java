package hr.fer.dippro.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface IMqttMessageParser {
    TopicData parseMessage(String topic, MqttMessage mqttMessage);
}
