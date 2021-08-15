package com.warehouse.service;

import com.warehouse.pojo.Sales;
import com.warehouse.pojo.Salesback;

import java.util.List;

public interface SalesService {
    public int getSalesCount();
    public int getTotalSales();
    public int getSalesState();
    public List<Sales> getRankState();
    public List<Sales> selectRunklist();
    public List<Sales> selectRunklistnumber();
    public List<Sales> selectnumber();

    public List<String> getCustomerName();
    public List<String> getGoodsName();
    public List<String> getSize(String goodsName);
    public List<Sales> selectAll(int page, int limit, String startTime, String endTime, Integer id);
    public int selectCount(String startTime, String endTime, Integer id);
    public int addSales(String customerName, String goodsName, String size, Integer number, double salePrice, String operateName, String salesTime, String payType, String remark);
    public int updateSales(int id, String customerName, String goodsName, String size, Integer number, double salePrice, String operateName, String salesTime, String payType, String remark);
    public int deleteSales(int id);

    public List<Salesback> selectBackSales(int page, int limit, String startTime, String endTime, Integer id);
    public int selectBackCount(String startTime, String endTime, Integer id);
    public int addBackSales(String customerName, String goodsName, String size, Integer number, String operateName, String salesbackTime, String payType, String reason);
    public int updateSalesBack(int id, String customerName, String goodsName, String size, Integer number, String operateName, String salesbackTime, String payType, String reason);
    public int deleteSalesBack(int id);
}
