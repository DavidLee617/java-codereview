package com.warehouse.controller;

import com.alibaba.fastjson.JSONObject;
import com.warehouse.pojo.Department;
import com.warehouse.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DapartmentController {
    @Autowired
    private DepartmentService departmentService;

    @RequestMapping("/Dapartment")
    @ResponseBody
    public String Dapartment(HttpServletRequest request){
        List<Department> departmentList = departmentService.getDepartmentList();
        int count = departmentList.size();
        Map<String,Object> map = new HashMap<>();
        map.put("code", 0);
        map.put("msg", "");
        map.put("count", count);
        map.put("data", departmentList);
        String str = JSONObject.toJSONString(map);
        return str;
    }

    @RequestMapping("/addDepartment")
    @ResponseBody
    private String addDepartment(Department department){
        departmentService.addDepartment(department);
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("code", 0);
        String res = JSONObject.toJSONString(map);
        return res;
    }

    @RequestMapping("/updateDepartment")
    @ResponseBody
    private String updateDepartment(Department department){
        departmentService.updateDepartment(department);
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("code", 0);
        String res = JSONObject.toJSONString(map);
        return res;
    }
    @RequestMapping("/deleteDepartment")
    @ResponseBody
    private String deleteDepartment(HttpServletRequest request){
        String ids = request.getParameter("idsStr");
        String[] str = ids.split(",");
        for (String id : str){
            departmentService.deleteDepartment(Integer.parseInt(id));
        }
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("code", 0);
        String res = JSONObject.toJSONString(map);
        return res;
    }
}
