package com.warehouse.dao;

import com.warehouse.pojo.Sales;
import com.warehouse.pojo.Salesback;

import java.util.List;

public interface SalesInterface {
    //拿到客户名字（添加/修改下拉选择）
    public List<String> getCustomerName();
    //拿到商品名字（同上）
    public List<String> getGoodsName();
    //拿到商品规格
    public List<String> getSize(String goodsName);
    //展示销售页面数据
    public List<Sales> selectAll(int page, int limit, String startTime, String endTime, Integer id);
    //得到页面数据数量
    public int selectCount(String startTime, String endTime, Integer id);
    //添加
    public int addSales(String customerName, String goodsName, String size, Integer number, double salePrice, String operateName, String salesTime, String payType, String remark);
    //修改
    public int updateSales(int id, String customerName, String goodsName, String size, Integer number, double salePrice, String operateName, String salesTime, String payType, String remark);
    //删除
    public int deleteSales(int id);

    //同上（是销售退货页面）
    public List<Salesback> selectBackSales(int page, int limit, String startTime, String endTime, Integer id);
    public int selectBackCount(String startTime, String endTime, Integer id);
    public int addBackSales(String customerName, String goodsName, String size, Integer number, String operateName, String salesbackTime, String payType, String reason);
    public int updateSalesBack(int id, String customerName, String goodsName, String size, Integer number, String operateName, String salesbackTime, String payType, String reason);
    public int deleteSalesBack(int id);
}
