package com.warehouse.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.warehouse.pojo.Sales;
import com.warehouse.service.SalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SalesController {
    @Autowired
    SalesService salesService;

    @RequestMapping("/getsalesCount")
    @ResponseBody
    public String getsalesCount(){
        return JSON.toJSONString(salesService.getSalesCount());
    }

    @RequestMapping("/getsalesnumSum")
    @ResponseBody
    public String getsalesnumSum(){
        return JSON.toJSONString(salesService.getTotalSales());
    }

    @RequestMapping("/getsalesNumber")
    @ResponseBody
    public String getsalesNumber(@RequestParam("page") int page, @RequestParam("limit") int limit){
        List<Sales> list = salesService.getRankState();
        JSONObject numberNum = new JSONObject();
        int count = salesService.getSalesCount();
        try{
            numberNum.put("code",0);
            //成功的状态码，默认：0
            numberNum.put("msg", "");
            numberNum.put("count",count);
            numberNum.put("data",list);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return numberNum.toJSONString();
    }

    @RequestMapping("/getnumber7")
    @ResponseBody
    public String getnumber7(){
        return JSON.toJSONString(salesService.getSalesState());
    }


    @RequestMapping("/selectSalesName")
    @ResponseBody
    public String selectName(){
//        List<Sales> list = salesService.selectRunklist();
        return JSON.toJSONString(salesService.selectRunklist());
    }

    @RequestMapping("/selectSalesnumber")
    @ResponseBody
    public String selectnumber(){
        List<Sales> list = salesService.selectRunklistnumber();
        JSONObject numberNum = new JSONObject();
        int count = salesService.getSalesCount();
        try{
            numberNum.put("code",0);
            //成功的状态码，默认：0
            numberNum.put("msg", "");
            numberNum.put("count",count);
            numberNum.put("data",list);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return numberNum.toJSONString();
    }

    @RequestMapping("/selectnumber")
    @ResponseBody
    public String selecview(){
        List<Sales> list = salesService.selectnumber();
        return JSON.toJSONString(list);
    }
}
