package com.warehouse.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.warehouse.pojo.Sales;
import com.warehouse.pojo.Salesback;
import com.warehouse.pojo.Staff;
import com.warehouse.service.SalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/salesController")
public class SalesController1 {
    @Autowired
    private SalesService service;

    JSONObject obj = new JSONObject();

    @RequestMapping("/allCus")
    @ResponseBody
    public String allCus(){
        return JSON.toJSONString(service.getCustomerName());
    }

    @RequestMapping("/allGoods")
    @ResponseBody
    public String allGoods(){
        return JSON.toJSONString(service.getGoodsName());
    }

    @RequestMapping("/allGoodsSize")
    @ResponseBody
    public String allGoodsSize(String name){
        return JSON.toJSONString(service.getSize(name));
    }

    @RequestMapping("/salesList")
    @ResponseBody
    public String getSalesList(@RequestParam("page") int page, @RequestParam("limit") int limit, @RequestParam(required = false)Integer id,  @RequestParam(required = false)String startTime,  @RequestParam(required = false)String endTime){
        JSONObject obj = new JSONObject();
        int page1 = (page-1)*10;
        List<Sales> list = service.selectAll(page1,limit,startTime,endTime,id);
        int count = service.selectCount(startTime,endTime,id);
        obj.put("code",0);
        obj.put("msg","");
        obj.put("count",count);
        obj.put("data",list);
        return obj.toJSONString();
    }

    @RequestMapping("/addSales")
    @ResponseBody
    public String addSales(String customerName, String goodsName, String size, Integer number, double salePrice, String salesTime, String payType, String remark, HttpServletRequest request){
        Staff staff = (Staff)  request.getSession().getAttribute("staff");
        String operateName = staff.getName();
        service.addSales(customerName,goodsName,size,number,salePrice,operateName,salesTime,payType,remark);
        obj.put("code",0);
        return obj.toJSONString();
    }

    @RequestMapping("/updateSales")
    @ResponseBody
    public String updateSales(int id, String customerName, String goodsName, String size, Integer number, double salePrice, String operateName, String salesTime, String payType, String remark){
        service.updateSales(id,customerName,goodsName,size,number,salePrice,operateName,salesTime,payType,remark);
        obj.put("code",0);
        return obj.toJSONString();
    }

    @RequestMapping("/deleteSales")
    @ResponseBody
    public String deleteSales(HttpServletRequest request){
        String ids = request.getParameter("idsStr");
        String[] str = ids.split(",");
        for(String id:str){
            int iid = Integer.parseInt(id);
            service.deleteSales(iid);
        }
        obj.put("code",0);
        return obj.toJSONString();
    }

    @RequestMapping("/getSalesBack")
    @ResponseBody
    public String getSalesBack(int page,int limit,@RequestParam(required = false)Integer id,  @RequestParam(required = false)String startTime,  @RequestParam(required = false)String endTime){
        JSONObject obj = new JSONObject();
        int page1 = (page-1)*10;
        List<Salesback> list = service.selectBackSales(page1,limit,startTime,endTime,id);
        int count = service.selectBackCount(startTime,endTime,id);
        obj.put("code",0);
        obj.put("msg","");
        obj.put("count",count);
        obj.put("data",list);
        return obj.toJSONString();
    }

    @RequestMapping("/addSalesBack")
    @ResponseBody
    public String addSalesBack(String customerName,String goodsName,String size,Integer number,String operateName,String salesbackTime,String payType,String reason,HttpServletRequest request){
        Staff staff = (Staff)  request.getSession().getAttribute("staff");
        String operateName1 = staff.getName();
        service.addBackSales(customerName,goodsName,size,number,operateName1,salesbackTime,payType,reason);
        obj.put("code",0);
        return obj.toJSONString();
    }

    @RequestMapping("/updateSalesBack")
    @ResponseBody
    public String updateSalesBack(int id,String customerName,String goodsName,String size,Integer number,String operateName,String salesbackTime,String payType,String reason){
        service.updateSalesBack(id,customerName,goodsName,size,number,operateName,salesbackTime,payType,reason);
        obj.put("code",0);
        return obj.toJSONString();
    }

    @RequestMapping("/deleteSalesBack")
    @ResponseBody
    public String deleteSalesBack(HttpServletRequest request){
        String ids = request.getParameter("idsStr");
        String[] str = ids.split(",");
        for(String id:str){
            int iid = Integer.parseInt(id);
            service.deleteSalesBack(iid);
        }
        obj.put("code",0);
        return obj.toJSONString();
    }
}
