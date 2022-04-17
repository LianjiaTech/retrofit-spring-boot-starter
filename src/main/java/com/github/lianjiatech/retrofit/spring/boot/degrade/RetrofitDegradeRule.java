package com.github.lianjiatech.retrofit.spring.boot.degrade;

/**
 * @author 陈添明
 */
public class RetrofitDegradeRule {

    private String resourceName;

    private Float count;

    private Integer timeWindow;

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public Float getCount() {
        return count;
    }

    public void setCount(Float count) {
        this.count = count;
    }

    public Integer getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(Integer timeWindow) {
        this.timeWindow = timeWindow;
    }

}
