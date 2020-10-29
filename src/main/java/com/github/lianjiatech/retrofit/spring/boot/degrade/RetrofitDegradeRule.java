package com.github.lianjiatech.retrofit.spring.boot.degrade;

/**
 * @author 陈添明
 */
public class RetrofitDegradeRule {

    private String resourceName;

    private double count;

    private int timeWindow;

    private DegradeStrategy degradeStrategy;

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    public int getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(int timeWindow) {
        this.timeWindow = timeWindow;
    }

    public DegradeStrategy getDegradeStrategy() {
        return degradeStrategy;
    }

    public void setDegradeStrategy(DegradeStrategy degradeStrategy) {
        this.degradeStrategy = degradeStrategy;
    }
}
