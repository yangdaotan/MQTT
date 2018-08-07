package com.mobike.mqtt.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/mqtt")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class MqttController {

    @GetMapping("/check/health")
    public String check() {
        return "ok";
    }

}
