package com.warehouse.controller;

import com.alibaba.fastjson.JSON;
import com.warehouse.pojo.Inport;
import com.warehouse.pojo.Staff;
import com.warehouse.service.InportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ Author
 */
@Controller
@RequestMapping("/pages/inport")
public class inportController {
    @Autowired
    private InportService inportService;

    @RequestMapping("/getnumber5")
    @ResponseBody
    public String getnumber5(){
        return JSON.toJSONString(inportService.getInportState());
    }
    @RequestMapping("/getProviderList")
    @ResponseBody
    public String getProviderList(
            @RequestParam(required = false,name = "startTime") String startTime,
            @RequestParam(required = false,name = "endTime") String endTime,
            @RequestParam(required = false,name = "ID") Integer ID,
            @RequestParam("page") Integer page, @RequestParam("limit") Integer size
    ){

        Map<String,Object> hashMap = new HashMap<>();
        List<Inport> list = inportService.getInportList(startTime,endTime,ID);
        List<Inport> hasLimitedList = new ArrayList<>();

        int begin = (page - 1) * size;
        int end = page * size;
        if (end > list.size())
            end = list.size();

        for (int i = begin; i < end; i++) {
            hasLimitedList.add(list.get(i));
        }
        int count = list.size();

        hashMap.put("code", 0);
        hashMap.put("msg", "");
        hashMap.put("count", count);
        hashMap.put("data", hasLimitedList);
        return JSON.toJSONString(hashMap);
    }

    @RequestMapping("/getApprovedInportList")
    @ResponseBody
    public String getApprovedInportList(
            @RequestParam(required = false,name = "startTime") String startTime,
            @RequestParam(required = false,name = "endTime") String endTime,
            @RequestParam(required = false,name = "ID") Integer ID,
            @RequestParam("page") Integer page, @RequestParam("limit") Integer size){

        HashMap<String,Object> hashMap = new HashMap<>();
        List<Inport> list = inportService.getApprovedInportList(startTime,endTime,ID);
        List<Inport> hasLimitedList = new ArrayList<>();

        int begin = (page - 1) * size;
        int end = page * size;
        if (end > list.size())
            end = list.size();

        for (int i = begin; i < end; i++) {
            hasLimitedList.add(list.get(i));
        }
        int count = list.size();

        hashMap.put("code", 0);
        hashMap.put("msg", "");
        hashMap.put("count", count);
        hashMap.put("data", hasLimitedList);
        return JSON.toJSONString(hashMap);
    }



    @RequestMapping("/add")
    public String add(){
        return "/pages/member/inport-add.html";
    }

    @RequestMapping("/edit")
    public String edit(Model model){
        return "/pages/member/inport-edit.html";
    }

    @RequestMapping("/del")
    public String del(){
        return "/pages/member/outport-edit.html";
    }

//   申请添加进货
    @RequestMapping(value = "/addGoods",method = RequestMethod.POST)
    @ResponseBody
    public String addGoods(@RequestParam String providerName, @RequestParam String goodsName,
                           @RequestParam String goodsSize, @RequestParam Integer goodsCount,
                           @RequestParam Double inportPrice, @RequestParam String payType,
                           @RequestParam String remark, @RequestParam String reason,
                           HttpSession session){
        Staff staff = (Staff) session.getAttribute("staff");
            String operateName = staff.getName();
            return JSON.toJSONString(inportService.inport(providerName,goodsName,
                    goodsSize,goodsCount,inportPrice,payType,operateName,remark,reason));

    }

//    动态模糊查询供货商名
    @RequestMapping(value = "/selectProviderName",method = RequestMethod.POST)
    @ResponseBody
    public String selectProviderName(@RequestParam String providerName){

        return JSON.toJSONString(inportService.getProviderNames(providerName));
    }

    //    动态模糊查询商品名
    @RequestMapping(value = "/selectGoodsName",method = RequestMethod.POST)
    @ResponseBody
    public String selectGoodsName(@RequestParam String goodsName){

        return JSON.toJSONString(inportService.getGoodsName(goodsName));
    }


    //    动态模糊查询商品名
    @RequestMapping(value = "/selectGoodsSize",method = RequestMethod.POST)
    @ResponseBody
    public String selectGoodsSize(@RequestParam String goodsName){

        return JSON.toJSONString(inportService.getGoodsSize(goodsName));
    }

//    根据id查询进货申请

    @RequestMapping(value = "/selectInportById",method = RequestMethod.POST)
    @ResponseBody
    public String selectInportById(@RequestParam Integer id){

        return JSON.toJSONString(inportService.getInportById(id));
    }

    //   更新进货表
    @RequestMapping(value = "/updateInport",method = RequestMethod.POST)
    @ResponseBody
    public String updateInport(@RequestParam String providerName, @RequestParam String goodsName,
                           @RequestParam String goodsSize, @RequestParam Integer goodsCount,
                           @RequestParam Double inportPrice, @RequestParam String payType,
                           @RequestParam String remark,@RequestParam Integer id, HttpSession session){
//            String operateName = (String) session.getAttribute("");
        String operateName = "张三";
        return JSON.toJSONString(inportService.updateInport(providerName,goodsName,goodsSize,goodsCount,
                inportPrice,payType,remark,id));

    }

//    批准进货
    @RequestMapping(value = "/approveAdd",method = RequestMethod.POST)
    @ResponseBody
    public String approveAdd(@RequestParam Integer id,HttpSession session){
        Staff staff = (Staff) session.getAttribute("staff");
        String approveName = staff.getName();
        return JSON.toJSONString(inportService.approveAdd(id,approveName));
    }

//    执行入库
    @RequestMapping(value = "/inportIntoWarehouse",method = RequestMethod.POST)
    @ResponseBody
    public String inportIntoWarehouse(@RequestParam Integer id ,HttpSession session){
        Staff staff = (Staff) session.getAttribute("staff");
        String warehouseName = staff.getName();
        return JSON.toJSONString(inportService.inportIntoWarehouse(id,warehouseName));
    }
}
