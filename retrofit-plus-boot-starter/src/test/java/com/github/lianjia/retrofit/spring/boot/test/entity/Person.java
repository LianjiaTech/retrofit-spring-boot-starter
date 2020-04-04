package com.github.lianjia.retrofit.spring.boot.test.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author 陈添明
 */
@Data
@Accessors(chain = true)
public class Person {

    private Long id;

    private String name;

    private Integer age;
}
