package hr.fer.dippro.mqtt;

public record TopicData(String topic, String deviceID, String payload, long timestamp) { }
