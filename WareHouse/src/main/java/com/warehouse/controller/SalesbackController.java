package com.warehouse.controller;

import com.alibaba.fastjson.JSON;
import com.warehouse.service.SalesbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SalesbackController {
    @Autowired
    SalesbackService salesbackService;

    @RequestMapping("/getnumber9")
    @ResponseBody
    public String getnumber9(){
        return JSON.toJSONString(salesbackService.getSalesbackState());
    }
}
