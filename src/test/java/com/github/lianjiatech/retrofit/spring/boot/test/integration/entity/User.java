package com.github.lianjiatech.retrofit.spring.boot.test.integration.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author 陈添明
 * @since 2023/12/17 2:53 下午
 */
@Data
@Accessors(chain = true)
public class User {

    private long id;

    private String name;

    private int age;

    private boolean male;
}
