package com.warehouse.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.warehouse.pojo.Ware;
import com.warehouse.service.WareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
public class WareController {
    @Autowired
    WareService wareService;

    @RequestMapping("/addW")
    public String add(){
        return "/pages/member/ware-add.html";
    }

    @RequestMapping("/editW")
    public String edit(){
        return "/pages/member/ware-edit.html";
    }

    @RequestMapping("/delW")
    public String del() {
        return "/pages/member/ware-edit.html";
    }
//全查仓库
    @RequestMapping("/ware")
    @ResponseBody
    public String getProvider(@RequestParam(required = false)Integer id, int page, int limit) {
        int page1 = (page-1)*limit;
        List<Ware> wareList = wareService.getWareList(page1,limit);
        JSONObject jobj = new JSONObject();
        int count = wareService.getWarecount();
            jobj.put("code",0);
            //成功的状态码，默认：0
            jobj.put("msg", "");
            jobj.put("count",count);
            jobj.put("data",wareList);

            if (id!=null){
                Ware ware = wareService.selectById(id);
                List<Ware> wareList1 = new ArrayList<>();
                wareList1.add(ware);
                jobj.put("code",0);
                //成功的状态码，默认：0
                jobj.put("msg", "");
                jobj.put("count",1);
                jobj.put("data",wareList1);
            }
        return JSON.toJSONString(jobj);
    }
//添加
    @RequestMapping("/addWare")
    @ResponseBody
    public String addWare(int count,String wareName){
        return JSON.toJSONString(wareService.addWL(count,wareName));
    }
}
