package com.github.lianjiatech.sample.server.entity;

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
