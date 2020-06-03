package com.github.lianjiatech.sample.server.controller;

import com.github.lianjiatech.sample.server.config.web.Result;
import com.github.lianjiatech.sample.server.entity.Person;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 陈添明
 */
@RestController
@RequestMapping("/api/test")
public class TestController {


    @GetMapping("/person")
    public Result getPersonById(@RequestParam("id") Long id) {
        Person person = new Person().setId(id)
                .setName("test")
                .setAge(10);
        return Result.ok(person);
    }


    @PostMapping("/savePerson")
    public Result savePerson(@RequestBody Person person) {
        System.out.println(person);
        return Result.ok();
    }

    @PostMapping("/error")
    public ResponseEntity error(@RequestBody Person person) {
        Map<String, Object> map = new HashMap<>(4);
        map.put("errorMessage", "我就是要手动报个错");
        map.put("errorCode", 500);
        return ResponseEntity.status(500).body(map);
    }

}
