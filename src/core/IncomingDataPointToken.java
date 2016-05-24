package net.opentsdb.core;

/**
 * Created by yaleiw on 5/23/16.
 */
public class IncomingDataPointToken {
    private String token;
    private IncomingDataPoint[] metrics;

    public IncomingDataPointToken() {}

    public IncomingDataPointToken(String token, IncomingDataPoint[] metrics) {
        this.token = token;
        this.metrics = metrics;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public IncomingDataPoint[] getMetrics() {
        return metrics;
    }

    public void setMetrics(IncomingDataPoint[] metrics) {
        this.metrics = metrics;
    }
}
