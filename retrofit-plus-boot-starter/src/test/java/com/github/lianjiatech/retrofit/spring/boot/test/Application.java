package com.github.lianjiatech.retrofit.spring.boot.test;

import com.github.lianjiatech.retrofit.plus.annotation.RetrofitScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author 陈添明
 */
@SpringBootApplication
@RetrofitScan("com.github.lianjiatech.retrofit.spring.boot.test.http")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
