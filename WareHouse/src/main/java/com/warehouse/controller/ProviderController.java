package com.warehouse.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.warehouse.pojo.Provider;
import com.warehouse.service.ProviderService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ProviderController {
    @Autowired
    ProviderService providerService;

    @RequestMapping("/getproviderCount")
    @ResponseBody
    public String getproviderCount(){
        return JSON.toJSONString(providerService.getProvidercount());
    }

    @RequestMapping("/getnumber3")
    @ResponseBody
    public String getnumber3(){
        return JSON.toJSONString(providerService.getProviderState());
    }

    @RequestMapping("/addP")
    public String add(){
        return "/pages/member/provder-add.html";
    }

    @RequestMapping("/editP")
    public String edit(){
        return "/pages/member/provder-edit.html";
    }

    @RequestMapping("/delP")
    public String del() {
        return "/pages/member/provder-edit.html";
    }
//全查供应商
    @RequestMapping("/provider")
    @ResponseBody
    public String getProvider(@RequestParam(required = false)String providername, int page, int limit) {
        int page1 = (page-1)*limit;
        List<Provider> providerList = providerService.getProviderList(page1,limit);
        JSONObject jobj = new JSONObject();
        int count = providerService.getProvidercount();
            jobj.put("code",0);
            //成功的状态码，默认：0
            jobj.put("msg", "");
            jobj.put("count",count);
            jobj.put("data",providerList);

        if (providername!=null){
            Provider provider = providerService.selectById(providername);
            List<Provider> providerList1 = new ArrayList<>();
            providerList1.add(provider);
            jobj.put("code",0);
            //成功的状态码，默认：0
            jobj.put("msg", "");
            jobj.put("count",1);
            jobj.put("data",providerList1);
        }

        return com.alibaba.fastjson.JSON.toJSONString(jobj);
    }

//  添加供货商
    @RequestMapping("/addProvider")
    @ResponseBody
    public String addProvider(
            @Param("providername") String providername,
            @Param("zip") String zip,
            @Param("address") String address,
            @Param("tel") String tel,
            @Param("contactname") String contactname,
            @Param("contacttel") String contacttel,
            @Param("bank") String bank,
            @Param("account") String account,
            @Param("email") String email,
            @Param("state") Integer state
    ){
        JSONObject jobj = new JSONObject();
        providerService.addProvider(providername,zip,address,tel
                ,contactname,contacttel,bank,account,email,state);
        jobj.put("code",0);
        return jobj.toJSONString();
    }


//    修改
    @RequestMapping("/updateProvider")
    @ResponseBody
    public String updateProvider(int id, String providername, String zip,
                                 String address, String tel, String contactname,
                                 String contacttel, String bank, String account,
                                 String email, int state){
        JSONObject jobj = new JSONObject();
        providerService.updateProvider(id,providername,zip,address,tel,contactname,
                contacttel,bank,account,email,state);
        jobj.put("code",0);
        return jobj.toJSONString();
    }
}
