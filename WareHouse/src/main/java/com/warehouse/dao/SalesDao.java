package com.warehouse.dao;

import com.warehouse.pojo.Sales;

import java.util.List;

public interface SalesDao {
    public int getSalesCount();

    public int getTotalSales();

    public int getSalesState();
    /*库存预警*/
    public List<Sales> getRankState();
    /*销售火爆商品名排行表*/
    public List<Sales> selectRunklist();
    /*销售火爆商品数量排行榜降序*/
    public List<Sales> selectRunklistnumber();
    /*销售总量图表*/
    public List<Sales> selectnumber();
}
