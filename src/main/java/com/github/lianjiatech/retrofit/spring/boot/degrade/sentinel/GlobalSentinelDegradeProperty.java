package com.github.lianjiatech.retrofit.spring.boot.degrade.sentinel;

import lombok.Data;

/**
 * 全局Sentinel降级配置
 * @author 陈添明
 * @since 2022/5/8 10:45 上午
 */
@Data
public class GlobalSentinelDegradeProperty {

    /**
     * 是否开启
     */
    private boolean enable = false;

    /**
     * 各降级策略对应的阈值。平均响应时间(ms)，异常比例(0-1)，异常数量(1-N)
     */
    private double count = 1000;

    /**
     * 熔断时长，单位为 s
     */
    private int timeWindow = 5;

    /**
     * 降级策略（0：平均响应时间；1：异常比例；2：异常数量）
     */
    private int grade = 0;
}
