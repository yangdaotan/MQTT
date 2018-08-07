package com.mobike.mqtt.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yangdaotan@mobike.com
 */
@RestController
public class IndexController {

    @RequestMapping("/")
    public String index(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
      return "ok";
    }
}
