package com.warehouse.dao;

import com.warehouse.pojo.Sales;
import com.warehouse.pojo.Salesback;

import java.util.List;

public interface SalesPlayInterface {
    //展示执行页面数据
    public List<Sales> getSalesList(int page, int limit, String startTime, String endTime, Integer id);
    public int getSalesListCount(String startTime, String endTime, Integer id);

    public List<Salesback> getSaleBackList(int page, int limit, String startTime, String endTime, Integer id);
    public int getSaleBackListCount(String startTime, String endTime, Integer id);

    //用于执行方法使用
    public int updateSalesMan(int id, String warehouseName);
    public int getSaleCount(int id);
    public Sales getSalesGoodsId(int id);
    public int updateGoodsCount(int id, int count);

    public int updateSalesBackMan(int id, String warehouseName);
    public int getSaleBackCount(int id);
    public Salesback getSalesBackGoodsId(int id);
    public int updateBackGoodsCount(int id, int count);

}
