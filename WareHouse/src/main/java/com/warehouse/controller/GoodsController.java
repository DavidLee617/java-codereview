package com.warehouse.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.warehouse.pojo.Goods;
import com.warehouse.service.GoodsService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

@Controller
public class GoodsController {
    @Autowired
    GoodsService goodsService;
    String goodsImgName = null;

    @RequestMapping("/addG")
    public String add(){
        return "/pages/member/goods-add.html";
    }

    @RequestMapping("/editG")
    public String edit(){
        return "/pages/member/goods-edit.html";
    }

    @RequestMapping("/delG")
    public String del() {
        return "/pages/member/goods-edit.html";
    }

    //    全查商品
    //模糊查询
    @RequestMapping("/goods")
    @ResponseBody
    public String getGoods(@RequestParam(required = false)String goodsName, int page, int limit) {
        int page1 = (page-1)*limit;
        List<Goods> goodslist = goodsService.getGoodslist(page1,limit);
        JSONObject jobj = new JSONObject();
        int count = goodsService.getGoodsCount();
            jobj.put("code",0);
            //成功的状态码，默认：0
            jobj.put("msg", "");
            jobj.put("count",count);
            jobj.put("data",goodslist);

            if (goodsName!= null){
                Goods goods = goodsService.selectById(goodsName);
                List<Goods> goodsList1 = new ArrayList<>();
                goodsList1.add(goods);
                jobj.put("code",0);
                //成功的状态码，默认：0
                jobj.put("msg", "");
                jobj.put("count",1);
                jobj.put("data",goodsList1);
            }

        return JSON.toJSONString(jobj);
    }

//    添加商品
    @RequestMapping("/addGoods")
    @ResponseBody
    public String addGoods(
            @Param("goodsName") String goodsName,
            @Param("producePlace") String producePlace,
            @Param("goodsType") String goodsType,
            @Param("size") String size,
            String type,
            @Param("productCode") String productCode,
            @Param("promitCode") String promitCode,
            @Param("description") String description,
            @Param("inportprice") double inportprice,
            @Param("salesprice") double salesprice,
            String providername,
            @Param("state") Integer state,
            @Param("number") Integer number,
            @Param("wareName") String wareName,
            @Param("locationName") String locationName
    ){
        JSONObject jobj = new JSONObject();
        int id = goodsService.getGoodsId(goodsImgName).getId();
        goodsService.updateGoods(id,goodsName,producePlace,goodsType,size,type,productCode,promitCode,description,inportprice,salesprice,providername,state,number,goodsImgName,wareName,locationName);
        jobj.put("code",0);
        return jobj.toJSONString();
    }

//    修改
    @RequestMapping(value = "/updateGoods",method = RequestMethod.POST)
    @ResponseBody
    public String updateGoods(int id, String goodsName, String producePlace, String goodsType, String size, String type, String productCode, String promitCode, String description, double inportprice, double salesprice, String providername, int state, int number, String wareName, String locationName){
        JSONObject jobj = new JSONObject();
        goodsService.updateGoods(id,goodsName,producePlace,goodsType,size,type,productCode,promitCode,description,inportprice,salesprice,providername,state,number,goodsImgName,wareName,locationName);
        jobj.put("code",0);
        return jobj.toJSONString();
    }


    //    删除
    @RequestMapping("/deleteGoods")
    @ResponseBody
    public String deleteGoods(HttpServletRequest request){
        JSONObject jobj = new JSONObject();
        String id1 = request.getParameter("idsStr");
        String [] str = id1.split(",");
        for (String id : str){
            int id2 = Integer.parseInt(id);
            goodsService.deleteGoods(id2);
        }
        jobj.put("code",0);
        return jobj.toJSONString();
    }
//查询仓库库位
    @RequestMapping("/selectWareLocation")
    @ResponseBody
    public String selectWareLocation(){
        return JSON.toJSONString(goodsService.selectWare());
    }

//查询类型
    @RequestMapping("/selectPackage")
    @ResponseBody
    public String selectPackage(){
        return JSON.toJSONString(goodsService.selectPackage());
    }
//查询供应商
    @RequestMapping("/selectProvider")
    @ResponseBody
    public String selectProvider(){
        return JSON.toJSONString(goodsService.selectProvider());
    }

    @RequestMapping("addGoodImg")
    @ResponseBody
    public Map upload(MultipartFile file, HttpServletRequest request){
        String prefix="";
        //保存上传
        OutputStream out = null;
        InputStream fileInput=null;
        try{
            if(file!=null){
                String originalName = file.getOriginalFilename();
                prefix=originalName.substring(originalName.lastIndexOf(".")+1);
                String uuid = UUID.randomUUID()+"";
                String filepath = "F:\\WareHouse\\src\\main\\resources\\static\\pages\\member\\img\\"+uuid+"." + prefix;
//                String filepath = request.getSession().getServletContext().getRealPath("static/pages/member/img/")+uuid+"." + prefix;
                goodsImgName ="img/"+uuid+"." + prefix;
                goodsService.addGoodsImg(goodsImgName);
                File files=new File(filepath);
                if(!files.getParentFile().exists()){
                    files.getParentFile().mkdirs();
                }
                file.transferTo(files);
                Map<String,Object> map2=new HashMap<>();
                Map<String,Object> map=new HashMap<>();
                map.put("code",0);
                map.put("msg","");
                map.put("data",map2);
                map2.put("src","/pages/member/img/"+uuid+"." + prefix);
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

    @RequestMapping("updateGoodImg")
    @ResponseBody
    public Map updateGoodImg(MultipartFile file, HttpServletRequest request){
        String prefix="";
        //保存上传
        OutputStream out = null;
        InputStream fileInput=null;
        try{
            if(file!=null){
                String originalName = file.getOriginalFilename();
                prefix=originalName.substring(originalName.lastIndexOf(".")+1);
                String uuid = UUID.randomUUID()+"";
                String filepath = "F:\\WareHouse\\src\\main\\resources\\static\\pages\\member\\img\\"+uuid+"." + prefix;
//                String filepath = request.getSession().getServletContext().getRealPath("static/pages/member/img/")+uuid+"." + prefix;
                goodsImgName ="img/"+uuid+"." + prefix;
                File files=new File(filepath);
                if(!files.getParentFile().exists()){
                    files.getParentFile().mkdirs();
                }
                file.transferTo(files);
                Map<String,Object> map2=new HashMap<>();
                Map<String,Object> map=new HashMap<>();
                map.put("code",0);
                map.put("msg","");
                map.put("data",map2);
                map2.put("src","/pages/member/img/"+uuid+"." + prefix);
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

    @RequestMapping("/getgoodsCount")
    @ResponseBody
    public String getgoodscount(){
        System.out.println(goodsService.getGoodsCount());
        return JSON.toJSONString(goodsService.getGoodsCount());
    }

    @RequestMapping("/getgoodsNumber")
    @ResponseBody
    public String getNumber(@RequestParam(required = false)int page, int limit){
        int page1 = (page-1)*limit;
        List<Goods> goodslist = goodsService.getGoodsNumber(page1,limit);
        JSONObject numberNum = new JSONObject();
        int count = goodsService.getGoodsCount();
        try{
            numberNum.put("code",0);
            //成功的状态码，默认：0
            numberNum.put("msg", "");
            numberNum.put("count",count);
            numberNum.put("data",goodslist);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return JSON.toJSONString(numberNum);
    }

    @RequestMapping("/getnumber8")
    @ResponseBody
    public String getnumber8(){
        return JSON.toJSONString(goodsService.getGoodsState());
    }
}
