package com.warehouse.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.warehouse.pojo.Notice;
import com.warehouse.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class NoticeController {
    @Autowired
    NoticeService noticeService;

    @RequestMapping("/getnoticeCount")
    @ResponseBody
    public String getGoodscount(){
        return JSON.toJSONString(noticeService.getNoticeCount());
    }

    @RequestMapping("/selectNoticeId")
    @ResponseBody
    public String selectId(Integer id){
        List<Notice> list = noticeService.selectNoticeId(id);
        return JSON.toJSONString(list);
    }
    @RequestMapping("/selectnoticelist")
    @ResponseBody
    public String getNotice(@RequestParam("page") int page, @RequestParam("limit") int limit, @RequestParam(required = false)String title,  @RequestParam(required = false)String startTime,  @RequestParam(required = false)String endTime){
        int page1 = (page-1)*limit;
        List<Notice> noticeList = noticeService.selectNoticeList(page1,limit,startTime,endTime,title);
        JSONObject no = new JSONObject();
        int count = noticeService.selectNoticeCount(startTime, endTime, title);
        no.put("code",0);
        no.put("msg","");
        no.put("count",count);
        no.put("data",noticeList);
        return no.toJSONString();
    }

    @RequestMapping("/addNotice")
    @ResponseBody
    public String addnotice(Integer id, String title, String context, String time, String operateName,HttpServletRequest request){
        JSONObject obj = new JSONObject();
        noticeService.addNotice(id, title, context, time, operateName);
        obj.put("code",0);
        return obj.toJSONString();
    }

    @RequestMapping("/updatenotice")
    @ResponseBody
    public String updatenotice(Integer id, String title, String context, String time, String operateName){
        JSONObject obj = new JSONObject();
        noticeService.updatenotice(id, title, context, time, operateName);
        obj.put("code",0);
        return obj.toJSONString();
    }


    @RequestMapping("/deleteNotice")
    @ResponseBody
    public String deleteNotice(HttpServletRequest request){
        System.out.print("1");
        JSONObject obj = new JSONObject();
        String ida = request.getParameter("idsStr");
        String[] str = ida.split(",");
        for(String id:str){
            System.out.print(id);
            int iid = Integer.parseInt(id);
            noticeService.deleteNotice(iid);
        }
        obj.put("code",0);
        return obj.toJSONString();
    }
}
