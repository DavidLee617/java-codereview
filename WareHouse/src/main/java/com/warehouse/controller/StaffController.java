package com.warehouse.controller;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.alibaba.fastjson.JSONObject;
import com.warehouse.pojo.Staff;
import com.warehouse.realm.nopassword.CustomToken;
import com.warehouse.service.LogService;
import com.warehouse.service.StaffService;
import com.warehouse.utils.EncryptUtil;
import com.warehouse.utils.IpUtil;
import org.apache.ibatis.annotations.Param;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class StaffController {
    @Autowired
    private StaffService staffService;

    @Autowired
    private LogService logService;

    @RequestMapping("/index1")
    @ResponseBody
    public String index1(){
        List<Staff> staffList = staffService.getStaffListByName("1001");
        String res = JSONObject.toJSONString(staffList);
        return res;
    }

    @RequestMapping("/index")
    public String index(){
        return "/index.html";
    }
    @RequestMapping("/toLogin")
    public String toLogin(){
        return "/pages/login.html";
    }
    @RequestMapping("/login")
    public String Login(String username,String password,HttpServletRequest request){
        Subject subject = SecurityUtils.getSubject();
        CustomToken token=new CustomToken(username, password);
        Staff staff = staffService.getStaff(username);
        request.getSession().setAttribute("staff",staff);
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = format.format(date);
        try {
            subject.login(token);
            logService.insertLog(staff.getName(),IpUtil.getIpAddr(request),dateStr);
            return "redirect:index";
        } catch (UnknownAccountException e) {
            return "redirect:toLogin";
        }catch (IncorrectCredentialsException e) {
            return "redirect:toLogin";
        }
    }

    @RequestMapping("/mobileLogin")
    public String mobileLogin(String tel,String captcha,HttpSession session,HttpServletRequest request){
        Staff staff = staffService.getStaffByTel(tel);
        Object verifyCode = session.getAttribute("verifyCode");
//        if(staff!= null && verifyCode.equals(captcha)){
            Subject subject = SecurityUtils.getSubject();
            CustomToken token=new CustomToken(tel);
            request.getSession().setAttribute("staff",staff);
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateStr = format.format(date);
            try {
                subject.login(token);
                logService.insertLog(staff.getName(),IpUtil.getIpAddr(request),dateStr);
                return "redirect:index";
            } catch (UnknownAccountException e) {
                return "redirect:toLogin";
            }catch (IncorrectCredentialsException e) {
                return "redirect:toLogin";
            }
//        }
    }

    @RequestMapping("/staff")
    @ResponseBody
    public String staff(@Param("page")Integer page, @Param("limit")Integer limit, @RequestParam(required = false)String startTime, @RequestParam(required = false)String endTime,@RequestParam(required = false)String jobNumber){
        Integer newPage=(page-1)*10;
        List<Staff> list = staffService.getStaffList(newPage,limit,jobNumber,startTime,endTime);
        int count = staffService.getStaffCount(jobNumber,startTime,endTime);
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("code", 0);
        map.put("msg", "");
        map.put("count", count);
        map.put("data", list);
        String res = JSONObject.toJSONString(map);
        return res;
    }

    @RequestMapping("/getSession")
    @ResponseBody
    public String getSession(HttpServletRequest request){
        Staff staff = (Staff) request.getSession().getAttribute("staff");
        return JSONObject.toJSONString(staff);
    }

    @RequestMapping("/getUserInfo")
    @ResponseBody
    public String getUserInfo(HttpServletRequest request){
        Staff staff = (Staff) request.getSession().getAttribute("staff");
        return JSONObject.toJSONString(staff);
    }

    @RequestMapping("/addStaff")
    @ResponseBody
    public String addStaff(Staff staff){
        staff.setPic("avatar.png");
        staff.setPassword(EncryptUtil.encrypt("123456"));
        staffService.addStaff(staff);
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("code", 0);
        String res = JSONObject.toJSONString(map);
        return res;
    }

    @RequestMapping("/updateStaff")
    @ResponseBody
    public String updateStaff(Staff staff){
        System.out.println(staff.getSex()+"---"+staff.getState());
        staffService.updateStaff(staff);
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("code", 0);
        String res = JSONObject.toJSONString(map);
        return res;
    }

    @RequestMapping("/updateStaffIndex")
    @ResponseBody
    public String updateStaffIndex(Staff staff,HttpServletRequest request){
        staffService.updateStaffIndex(staff);
        Staff newStaff = staffService.getStaff(staff.getJobNumber());
        request.getSession().setAttribute("staff",newStaff);
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("code", 0);
        String res = JSONObject.toJSONString(map);
        return res;
    }

    @RequestMapping("/deleteStaff")
    @ResponseBody
    public String deleteStaff(HttpServletRequest request){
        String ids = request.getParameter("idsStr");
        String[] str = ids.split(",");
        for (String id : str){
            staffService.deleteStaff(Integer.parseInt(id));
        }
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("code", 0);
        String res = JSONObject.toJSONString(map);
        return res;
    }

    @RequestMapping("/updatePwd")
    @ResponseBody
    public String updatePwd(Integer id,String password,HttpServletRequest request){
        staffService.updatePwd(id,EncryptUtil.encrypt(password));
        Staff newStaff = staffService.getStaffById(id);
        request.getSession().setAttribute("staff",newStaff);
        return JSONObject.toJSONString(0);
    }

    @RequestMapping("upload")
    @ResponseBody
    public Map upload(MultipartFile file, HttpServletRequest request){
        Staff staff = (Staff) request.getSession().getAttribute("staff");
        String prefix="";
        String dateStr="";
        //保存上传
        OutputStream out = null;
        InputStream fileInput=null;
        try{
            if(file!=null){
                String originalName = file.getOriginalFilename();
                prefix=originalName.substring(originalName.lastIndexOf(".")+1);
                Date date = new Date();
                String uuid = UUID.randomUUID()+"";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateStr = simpleDateFormat.format(date);
                String filepath = "F:\\WareHouse\\src\\main\\resources\\static\\images\\" + dateStr+"\\"+uuid+"." + prefix;
//                String filepath = request.getSession().getServletContext().getRealPath("static/images/") + dateStr+"\\"+uuid+"." + prefix;

                File files=new File(filepath);
                staff.setPic(dateStr+"\\"+uuid+"." + prefix);
                staffService.updateStaff(staff);
                if(!files.getParentFile().exists()){
                    files.getParentFile().mkdirs();
                }
                file.transferTo(files);
                Map<String,Object> map2=new HashMap<>();
                Map<String,Object> map=new HashMap<>();
                map.put("code",0);
                map.put("msg","");
                map.put("data",map2);
                map2.put("src","/images/"+ dateStr+"/"+uuid+"." + prefix);
                return map;
            }

        }catch (Exception e){
        }finally{
            try {
                if(out!=null){
                    out.close();
                }
                if(fileInput!=null){
                    fileInput.close();
                }
            } catch (IOException e) {
            }
        }
        Map<String,Object> map=new HashMap<>();
        map.put("code",1);
        map.put("msg","");
        return map;

    }

    @RequestMapping("/unAuth")
    public String unAuth(){
        return "/pages/500.html";
    }

    @RequestMapping("/forget")
    public String forget(){
        return "/pages/forget.html";
    }

    @RequestMapping("/tel")
    public String tel(){
        return "/pages/tel.html";
    }

    @RequestMapping("/verifyUser")
    @ResponseBody
    public String verifyUser(String jobNumber,String tel,String captcha,HttpSession session){
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("code", 0);
        Staff staff = staffService.getStaff(jobNumber);
        Object verifyCode = session.getAttribute("verifyCode");
        if(staff.getTel().equals(tel) && verifyCode.equals(captcha)){
            map.put("data", 1);
            session.setAttribute("staff",staff);
        }else if(!staff.getTel().equals(tel)){
            map.put("data", 2);
        }else if(!verifyCode.equals(captcha)){
            map.put("data", 3);
        }
        String res = JSONObject.toJSONString(map);
        return res;
    }

    @RequestMapping("/sendCaptcha")
    @ResponseBody
    public String sendCaptcha(HttpServletRequest httpServletRequest, String tel){
        //生成4位验证码
        String verifyCode = String.valueOf(new Random().nextInt(8999) + 1000);
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "LTAI4Fsb3uubPK3wiM9k61Rc", "G77b7otvbxTMjuCCJmFNnlumCyyV93");
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", tel);
        request.putQueryParameter("SignName", "乘风物流仓储管理系统");
        request.putQueryParameter("TemplateCode", "SMS_183160713");
        request.putQueryParameter("TemplateParam", "{\"code\":\""+verifyCode+"\"}");
        try {
            CommonResponse response = client.getCommonResponse(request);
            HttpSession session = httpServletRequest.getSession();
            session.setAttribute("verifyCode",verifyCode);
            session.setAttribute("verifyCodeCreateTime",System.currentTimeMillis());
        } catch (ClientException e) {
            e.printStackTrace();
        }
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("code", 0);
        String res = JSONObject.toJSONString(map);
        return res;
    }
    @RequestMapping("/resetPassword")
    @ResponseBody
    public String resetPassword(String password,HttpServletRequest request){
        Staff staff = (Staff) request.getSession().getAttribute("staff");
        staffService.updatePwd(staff.getId(),EncryptUtil.encrypt(password));
        request.getSession().removeAttribute("staff");
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("code", 0);
        map.put("msg", "");
        String res = JSONObject.toJSONString(map);
        return res;
    }

    @RequestMapping("/url")
    @ResponseBody
    public String url(HttpServletRequest request){
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("code", 0);
        Staff staff = (Staff) request.getSession().getAttribute("staff");
        map.put("data", staff.getRoleId());
        String res = JSONObject.toJSONString(map);
        return res;
    }

    @RequestMapping("/getMd5")
    @ResponseBody
    public String getMd5(String password){
        String str = EncryptUtil.encrypt(password);
        return JSONObject.toJSONString(str);
    }
}
