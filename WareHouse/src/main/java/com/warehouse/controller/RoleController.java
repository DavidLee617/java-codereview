package com.warehouse.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.warehouse.pojo.Role;
import com.warehouse.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class RoleController {
    @Autowired
    private RoleService roleService;

    @RequestMapping("/getnumber1")
    @ResponseBody
    public String getnumber1(){
        return JSON.toJSONString(roleService.getRoleState());
    }

    @RequestMapping("/Role")
    @ResponseBody
    private String role(){
        List<Role> RoleList = roleService.getRoleList();
        int count = RoleList.size();
        Map<String,Object> roleMap = new HashMap<>();
        roleMap.put("code", 0);
        roleMap.put("msg", "");
        roleMap.put("count", count);
        roleMap.put("data", RoleList);
        String roleRes = JSONObject.toJSONString(roleMap);
        return roleRes;
    }

    @RequestMapping("/addRole")
    @ResponseBody
    private String addRole(Role role){
        roleService.addRole(role);
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("code", 0);
        String res = JSONObject.toJSONString(map);
        return res;
    }

    @RequestMapping("/addPermission")
    @ResponseBody
    private String addPermission(int[] pname,int roleId){
        for (int i = 0; i < pname.length; i++) {
            roleService.addPermission(roleId,pname[i]);
        }
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("code", 0);
        String res = JSONObject.toJSONString(map);
        return res;
    }

    @RequestMapping("/updateRole")
    @ResponseBody
    private String updateRole(Role role){
        roleService.updateRole(role);
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("code", 0);
        String res = JSONObject.toJSONString(map);
        return res;
    }
    @RequestMapping("/deleteRole")
    @ResponseBody
    private String deleteRole(HttpServletRequest request){
        String ids = request.getParameter("idsStr");
        String[] str = ids.split(",");
        for (String id : str){
            roleService.deleteRole(Integer.parseInt(id));
        }
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("code", 0);
        String res = JSONObject.toJSONString(map);
        return res;
    }
}
