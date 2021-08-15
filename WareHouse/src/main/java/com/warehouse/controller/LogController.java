package com.warehouse.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.warehouse.pojo.Log;
import com.warehouse.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class LogController {
    @Autowired
    LogService logService;

    @RequestMapping("/getlogcount")
    @ResponseBody
    public String getlogcount(){
        return JSON.toJSONString(logService.getLogCount());
    }

    @RequestMapping("/getlogList")
    @ResponseBody
    public String getLog(@RequestParam("page") int page, @RequestParam("limit") int limit, @RequestParam(required = false)String name,  @RequestParam(required = false)String startTime,  @RequestParam(required = false)String endTime){
        JSONObject no = new JSONObject();
        int page1 = (page-1)*limit;
        List<Log> logList = logService.getLogList(page1,limit,startTime,endTime,name);
        int count = logService.selectCount(startTime, endTime, name);
        no.put("code",0);
        no.put("msg","");
        no.put("count",count);
        no.put("data",logList);
        return no.toJSONString();
    }

}
