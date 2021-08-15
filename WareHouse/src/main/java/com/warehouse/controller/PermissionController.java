package com.warehouse.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.warehouse.pojo.Permission;
import com.warehouse.service.PermissionService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PermissionController {
    @Autowired
    private PermissionService permissionService;

    @RequestMapping("/getnumber2")
    @ResponseBody
    public String getnumber2(){
        return JSON.toJSONString(permissionService.getPermissionState());
    }

    @RequestMapping("/Permission")
    @ResponseBody
    private String Permission(@Param("page")Integer page, @Param("limit")Integer limit){
        Integer newPage=(page-1)*10;
        List<Permission> permissionList = permissionService.getPermissionList(newPage,limit);
        int count = permissionService.getPermissionCount();
        Map<String,Object> roleMap = new HashMap<>();
        roleMap.put("code", 0);
        roleMap.put("msg", "");
        roleMap.put("count", count);
        roleMap.put("data", permissionList);
        String permissionRes = JSONObject.toJSONString(roleMap);
        return permissionRes;
    }

    @RequestMapping("/getAuth")
    @ResponseBody
    private String getPermission(@Param("rid")Integer rid){
        List<Permission> permissionList = permissionService.getPermissionListByRid(rid);
        int count = permissionList.size();
        Map<String,Object> roleMap = new HashMap<>();
        roleMap.put("code", 0);
        roleMap.put("msg", "");
        roleMap.put("count", count);
        roleMap.put("data", permissionList);
        String permissionRes = JSONObject.toJSONString(roleMap);
        return permissionRes;
    }
}
