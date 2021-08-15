package com.warehouse.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.warehouse.pojo.Customar;
import com.warehouse.service.CustomarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CustomarController {
    @Autowired
    CustomarService customarService;

    @RequestMapping("/addC")
    public String add(){
        return "/pages/member/customar-add.html";
    }

    @RequestMapping("/editC")
    public String edit(){
        return "/pages/member/customar-edit.html";
    }

    @RequestMapping("/delC")
    public String del() {
        return "/pages/member/customar-edit.html";
    }

//  客户全部查询
//模糊查询
    @RequestMapping("/customar")
    @ResponseBody
    public String getCustomar(@RequestParam(required = false)String customerName, int page, int limit) {
        int page1 = (page-1)*limit;
        List<Customar> customarList = customarService.getCustomarList(page1,limit);
        JSONObject jobj = new JSONObject();
        int count = customarService.getCustomarcount();
            jobj.put("code",0);
            //成功的状态码，默认：0
            jobj.put("msg", "");
            jobj.put("count",count);
            jobj.put("data",customarList);

            if (customerName!=null){
                Customar customar = customarService.selectById(customerName);
                List<Customar> customarList1 = new ArrayList<>();
                customarList1.add(customar);
                jobj.put("code",0);
                jobj.put("msg","");
                jobj.put("count",1);
                jobj.put("data",customarList1);
            }

        return com.alibaba.fastjson.JSON.toJSONString(jobj);
    }

//    添加客户
    @RequestMapping("/addCustomar")
    @ResponseBody
    public String addCustomar(
                    String customerName,
                    String zip,
                    String tel,
                    String address,
                    String contactName,
                    String contactTel,
                    String email,
                    String bank,
                    String account,
                    Integer state
    ){
        JSONObject jobj = new JSONObject();
        customarService.addCustomar(customerName,zip,tel,address,contactName,contactTel,email,bank,account,state);
        jobj.put("code",0);
        return jobj.toJSONString();
    }

//    删除
    @RequestMapping("/deleteCustomar")
    @ResponseBody
    public String deleteCustomar(HttpServletRequest request){
        JSONObject jobj = new JSONObject();
        String id1 = request.getParameter("idsStr");
        String[] str = id1.split(",");
        for (String id : str){
            int id2 = Integer.parseInt(id);
            customarService.deleteCustomar(id2);
        }
        jobj.put("code",0);
        return jobj.toJSONString();
    }

//修改
    @RequestMapping("/updateCustomar")
    @ResponseBody
    public String updateCustomar(int id, String customerName, String zip, String tel, String address, String contactName, String contactTel, String bank, String account, String email, int state){
        JSONObject jobj = new JSONObject();
        customarService.updateCustomer(id,customerName,zip,tel,address,contactName,contactTel,bank,account,email,state);
        jobj.put("code",0);
        return jobj.toJSONString();
    }


    @RequestMapping("pages/console")
    public String customal(){
        return "/pages/console.html";
    }

    @RequestMapping("/getcustomarnumCount")
    @ResponseBody
    public String getsalesnumCount(){
        return JSON.toJSONString(customarService.getCustomarcount());
    }

    @RequestMapping("/getnumber4")
    @ResponseBody
    public String getnumber4(){
        return JSON.toJSONString(customarService.getcustomarState());
    }

}
