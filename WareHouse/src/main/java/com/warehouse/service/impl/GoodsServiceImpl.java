package com.warehouse.service.impl;

import com.warehouse.dao.GoodsDao;
import com.warehouse.pojo.*;
import com.warehouse.pojo.Package;
import com.warehouse.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("goodsService")
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private GoodsDao goodsDao;

    @Override
    public List<Goods> getGoodsNumber(int page, int limit) {
        return goodsDao.getGoodsNumber(page, limit);
    }

    @Override
    public int getGoodsState() {
        return goodsDao.getGoodsState();
    }

    @Override
    public int addGoodsImg(String goodImg) {
        return goodsDao.addGoodsImg(goodImg);
    }

    @Override
    public Goods getGoodsId(String goodImg) {
        return goodsDao.getGoodsId(goodImg);
    }

    //全查商品
    public List<Goods> getGoodslist(int page, int limit) {
        return goodsDao.getGoodsList(page,limit);
    }
//查看商品数量数据
    public int getGoodsCount() {
        return goodsDao.getGoodscount();
    }

    public int updateGoods(int id, String goodsName, String producePlace, String goodsType, String size, String type, String productCode, String promitCode, String description, double inportprice, double salesprice, String providername, int state, int number, String goodsImg, String wareName, String locationName) {
        return goodsDao.updateGoods(id,goodsName,producePlace,goodsType,size,type,promitCode,promitCode,description,inportprice,salesprice,providername,state,number,goodsImg,wareName,locationName);
    }

    //修改商品
//添加商品
    public boolean addGoods(String goodsName, String producePlace, String goodsType, String size, Integer packageId,String type, String productCode, String promitCode, String description, double inportprice, double salesprice, Integer providerId,String providername, Integer state, Integer number, String goodsImg,Integer wareId, String wareName,Integer locationId,String locationName) {
        return goodsDao.addGoods(goodsName,producePlace,goodsType,size,packageId,type,promitCode,promitCode,description,inportprice,salesprice,providerId,providername,state,number,goodsImg,wareId,wareName,locationId,locationName) == 1;
    }

//    删除
    public int deleteGoods(int id) {
        return goodsDao.deleteGoods(id);
    }

//    模糊
    public Goods selectById(String goodsName) {
        return goodsDao.selectById(goodsName);
    }

    public Map<String,List> selectWare() {
        Map<String,List> map = new HashMap<>();
        List<Ware> list =  goodsDao.selectWare();
        List<Location> list1 =  goodsDao.selectLocation();
        map.put("ware",list);
        map.put("location",list1);
        return map;
    }
    //查询类型
    public Map<String, List> selectPackage() {
        Map<String,List> map = new HashMap<>();
        List<Package> list = goodsDao.selectPackage();
        map.put("package",list);
        return map;
    }
    //查询供应商
    public Map<String, List> selectProvider() {
        Map<String,List> map = new HashMap<>();
        List<Provider> list = goodsDao.selectProvider();
        map.put("provider",list);
        return map;
    }

}
