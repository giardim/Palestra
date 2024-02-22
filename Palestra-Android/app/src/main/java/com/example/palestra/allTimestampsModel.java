package com.example.palestra;

public class allTimestampsModel {
    private String timestamp;
    private String stats;

    public allTimestampsModel(String timestamp, String stats) {
        this.timestamp = timestamp;
        this.stats = stats;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getStats(){
        return stats;
    }
}
