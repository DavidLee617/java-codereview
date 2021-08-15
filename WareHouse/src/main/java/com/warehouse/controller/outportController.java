package com.warehouse.controller;

import com.alibaba.fastjson.JSON;
import com.warehouse.pojo.Inport;
import com.warehouse.pojo.Outport;
import com.warehouse.pojo.Staff;
import com.warehouse.service.OutportService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @ Author
 */
@Controller
@RequestMapping("/pages/outport")
public class outportController {
    @Autowired
    private OutportService outportService;

    @RequestMapping("/getnumber6")
    @ResponseBody
    public String getnumber6(){
        return JSON.toJSONString(outportService.getOutportState());
    }

    @RequestMapping("/getOutportList")
    @ResponseBody
    public String getOutportList(
            @RequestParam(required = false,name = "startTime") String startTime,
            @RequestParam(required = false,name = "endTime") String endTime,
            @RequestParam(required = false,name = "ID") Integer ID,
            @RequestParam("page") Integer page, @RequestParam("limit") Integer size){
        HashMap<String,Object> hashMap = new HashMap<>();
        List<Outport> list = outportService.getOutportList(startTime,endTime,ID);
        List<Outport> hasLimitedList = new ArrayList<>();
        int count = list.size();

        int begin = (page - 1) * size;
        int end = page * size;
        if (end > list.size())
            end = list.size();

        for (int i = begin; i < end; i++) {
            hasLimitedList.add(list.get(i));
        }
        hashMap.put("code", 0);
        hashMap.put("msg", "");
        hashMap.put("count", count);
        hashMap.put("data", hasLimitedList);
        return JSON.toJSONString(hashMap);
    }


    @RequestMapping("/getApprovedOutportList")
    @ResponseBody
    public String getApprovedOutportList(
            @RequestParam(required = false,name = "startTime") String startTime,
            @RequestParam(required = false,name = "endTime") String endTime,
            @RequestParam(required = false,name = "ID") Integer ID,
            @RequestParam("page") Integer page, @RequestParam("limit") Integer size){
        HashMap<String,Object> hashMap = new HashMap<>();
        List<Outport> list = outportService.getApprovedOutportList(startTime,endTime,ID);
        List<Outport> hasLimitedList = new ArrayList<>();
        int count = list.size();

        int begin = (page - 1) * size;
        int end = page * size;
        if (end > list.size())
            end = list.size();

        for (int i = begin; i < end; i++) {
            hasLimitedList.add(list.get(i));
        }
        hashMap.put("code", 0);
        hashMap.put("msg", "");
        hashMap.put("count", count);
        hashMap.put("data", hasLimitedList);
        return JSON.toJSONString(hashMap);
    }

    //   申请退货
    @RequestMapping(value = "/outGoods",method = RequestMethod.POST)
    @ResponseBody
    public String addGoods(@RequestParam String providerName, @RequestParam String goodsName,
                           @RequestParam String goodsSize, @RequestParam Integer number,
                           @RequestParam Double outputPrice, @RequestParam String payType,
                           @RequestParam String reason, HttpSession session){
        Staff staff = (Staff) session.getAttribute("staff");
            String operateName = staff.getName();
        return JSON.toJSONString(outportService.outport(providerName,goodsName,goodsSize,number,
                outputPrice,payType,operateName,reason));

    }

    @RequestMapping("/approveOut")
    @ResponseBody
    public String approveOut(@RequestParam Integer id,HttpSession session){
        Staff staff = (Staff) session.getAttribute("staff");
                    String approveName = staff.getName();
        return JSON.toJSONString(outportService.approveOut(id,approveName));
    }

    //    执行出库
    @RequestMapping(value = "/outportFromWarehouse",method = RequestMethod.POST)
    @ResponseBody
    public String inportIntoWarehouse(@RequestParam Integer id ,HttpSession session){
        Staff staff = (Staff) session.getAttribute("staff");
                    String warehouseName = staff.getName();
        return JSON.toJSONString(outportService.outportFromWarehouse(id,warehouseName));
    }

}
