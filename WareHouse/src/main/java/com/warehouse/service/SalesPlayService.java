package com.warehouse.service;

import com.warehouse.pojo.Sales;
import com.warehouse.pojo.Salesback;

import java.util.List;

public interface SalesPlayService {
    public List<Sales> getSalesList(int page, int limit, String startTime, String endTime, Integer id);
    public int getSalesListCount(String startTime, String endTime, Integer id);

    public List<Salesback> getSaleBackList(int page, int limit, String startTime, String endTime, Integer id);
    public int getSaleBackListCount(String startTime, String endTime, Integer id);

    public int updateSalesMan(int id, String warehouseName);
    public int getSaleCount(int id);
    public Sales getSalesGoodsId(int id);
    public int updateGoodsCount(int id, int count);

    public int updateSalesBackMan(int id, String warehouseName);
    public int getSaleBackCount(int id);
    public Salesback getSalesBackGoodsId(int id);
    public int updateBackGoodsCount(int id, int count);
}
