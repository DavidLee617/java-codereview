package com.warehouse.controller;

import com.alibaba.fastjson.JSONObject;
import com.warehouse.pojo.Sales;
import com.warehouse.pojo.Salesback;
import com.warehouse.pojo.Staff;
import com.warehouse.service.SalesPlayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/SalesPlayController")
public class SalesPlayController {
    @Autowired
    private SalesPlayService service;

    JSONObject obj = new JSONObject();

    @RequestMapping("/salesFromList")
    @ResponseBody
    public String salesFromList(int page, int limit, @RequestParam(required = false)Integer id, @RequestParam(required = false)String startTime, @RequestParam(required = false)String endTime){
        JSONObject obj = new JSONObject();
        int page1 = (page-1)*10;
        List<Sales> list = service.getSalesList(page1,limit,startTime,endTime,id);
        int count = service.getSalesListCount(startTime,endTime,id);
        obj.put("code",0);
        obj.put("msg","");
        obj.put("count",count);
        obj.put("data",list);
        return obj.toJSONString();
    }

    @RequestMapping("/salesBackFromList")
    @ResponseBody
    public String salesBackFromList(int page,int limit, @RequestParam(required = false)Integer id, @RequestParam(required = false)String startTime, @RequestParam(required = false)String endTime){
        JSONObject obj = new JSONObject();
        int page1 = (page-1)*10;
        List<Salesback> list1 = service.getSaleBackList(page1,limit,startTime,endTime,id);
        int count = service.getSaleBackListCount(startTime,endTime,id);
        obj.put("code",0);
        obj.put("msg","");
        obj.put("count",count);
        obj.put("data",list1);
        return obj.toJSONString();
    }

    @RequestMapping("/doSalesOutput")
    @ResponseBody
    public String doSalesOutput(HttpServletRequest request){
        Staff staff = (Staff) request.getSession().getAttribute("staff");
        String warehouseName = staff.getName();
        String ids = request.getParameter("idsStr");
        String[] str = ids.split(",");
        for(String id:str){
            int iid = Integer.parseInt(id);
            service.updateSalesMan(iid,warehouseName);
            int count = service.getSaleCount(iid);
            Sales sales = service.getSalesGoodsId(iid);
            service.updateGoodsCount(sales.getGoodsId(),count);
        }
        obj.put("code",0);
        return obj.toJSONString();
    }

    @RequestMapping("/doSalesInput")
    @ResponseBody
    public String doSalesInput(HttpServletRequest request){
        Staff staff = (Staff) request.getSession().getAttribute("staff");
        String warehouseName = staff.getName();
        String ids = request.getParameter("idsStr");
        String[] str = ids.split(",");
        for(String id:str){
            int iid = Integer.parseInt(id);
            service.updateSalesBackMan(iid,warehouseName);
            int count = service.getSaleBackCount(iid);
            Salesback salesback = service.getSalesBackGoodsId(iid);
            service.updateBackGoodsCount(salesback.getGoodsId(),count);
        }
        obj.put("code",0);
        return obj.toJSONString();
    }

}
