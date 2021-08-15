package com.warehouse.service.impl;

import com.warehouse.dao.SalesPlayInterface;
import com.warehouse.pojo.Sales;
import com.warehouse.pojo.Salesback;
import com.warehouse.service.SalesPlayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("SalesPlayService")
public class SalesPlayServiceImpl implements SalesPlayService {

    @Autowired
    private SalesPlayInterface dao;

    public List<Sales> getSalesList(int page, int limit,String startTime,String endTime,Integer id) {
        return dao.getSalesList(page,limit,startTime,endTime,id);
    }

    public int getSalesListCount(String startTime,String endTime,Integer id) {
        return dao.getSalesListCount(startTime,endTime,id);
    }



    public List<Salesback> getSaleBackList(int page, int limit,String startTime,String endTime,Integer id) {
        return dao.getSaleBackList(page,limit,startTime,endTime,id);
    }

    public int getSaleBackListCount(String startTime,String endTime,Integer id) {
        return dao.getSaleBackListCount(startTime,endTime,id);
    }


    public int updateSalesMan(int id, String warehouseName) {
        return dao.updateSalesMan(id,warehouseName);
    }

    public int getSaleCount(int id) {
        return dao.getSaleCount(id);
    }

    public Sales getSalesGoodsId(int id) {
        return dao.getSalesGoodsId(id);
    }

    public int updateGoodsCount(int id, int count) {
        return dao.updateGoodsCount(id,count);
    }

    public int updateSalesBackMan(int id, String warehouseName) {
        return dao.updateSalesBackMan(id,warehouseName);
    }

    public int getSaleBackCount(int id) {
        return dao.getSaleBackCount(id);
    }

    public Salesback getSalesBackGoodsId(int id) {
        return dao.getSalesBackGoodsId(id);
    }

    public int updateBackGoodsCount(int id, int count) {
        return dao.updateBackGoodsCount(id,count);
    }


}
