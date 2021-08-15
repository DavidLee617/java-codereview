package com.warehouse.service.impl;

import com.warehouse.dao.SalesDao;
import com.warehouse.dao.SalesInterface;
import com.warehouse.pojo.Sales;
import com.warehouse.pojo.Salesback;
import com.warehouse.service.SalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("SalesService")
public class SalesServiceImpl implements SalesService {
    @Autowired
    private SalesInterface dao;
    @Autowired
    private SalesDao salesDao;

    @Override
    public int getSalesCount() {
        return salesDao.getSalesCount();
    }

    @Override
    public int getTotalSales() {
        return salesDao.getTotalSales();
    }

    @Override
    public int getSalesState() {
        return salesDao.getSalesState();
    }

    @Override
    public List<Sales> getRankState() {
        return salesDao.getRankState();
    }

    @Override
    public List<Sales> selectRunklist() {
        return salesDao.selectRunklist();
    }

    @Override
    public List<Sales> selectRunklistnumber() {
        return salesDao.selectRunklistnumber();
    }

    @Override
    public List<Sales> selectnumber() {
        return salesDao.selectnumber();
    }

    public List<String> getCustomerName() {
        return dao.getCustomerName();
    }

    public List<String> getGoodsName() {
        return dao.getGoodsName();
    }

    public List<String> getSize(String goodsName) {
        return dao.getSize(goodsName);
    }

    public List<Sales> selectAll(int page, int limit, String startTime, String endTime, Integer id) {
        return dao.selectAll(page,limit,startTime,endTime,id);
    }

    public int selectCount(String startTime,String endTime,Integer id) {
        return dao.selectCount(startTime,endTime,id);
    }

    public int addSales(String customerName, String goodsName, String size, Integer number, double salePrice, String operateName, String salesTime, String payType, String remark) {
        return dao.addSales(customerName,goodsName,size,number,salePrice,operateName,salesTime,payType,remark);
    }

    public int updateSales(int id, String customerName, String goodsName, String size, Integer number, double salePrice, String operateName, String salesTime, String payType, String remark) {
        return dao.updateSales(id,customerName,goodsName,size,number,salePrice,operateName,salesTime,payType,remark);
    }

    public int deleteSales(int id) {
        return dao.deleteSales(id);
    }





    public List<Salesback> selectBackSales(int page, int limit, String startTime, String endTime, Integer id) {
        return dao.selectBackSales(page,limit,startTime,endTime,id);
    }

    public int selectBackCount(String startTime,String endTime,Integer id) {
        return dao.selectBackCount(startTime,endTime,id);
    }

    public int addBackSales(String customerName, String goodsName, String size, Integer number, String operateName, String salesbackTime, String payType, String reason) {
        return dao.addBackSales(customerName,goodsName,size,number,operateName,salesbackTime,payType,reason);
    }

    public int updateSalesBack(int id, String customerName, String goodsName, String size, Integer number, String operateName, String salesbackTime, String payType, String reason) {
        return dao.updateSalesBack(id,customerName,goodsName,size,number,operateName,salesbackTime,payType,reason);
    }

    public int deleteSalesBack(int id) {
        return dao.deleteSalesBack(id);
    }
}
