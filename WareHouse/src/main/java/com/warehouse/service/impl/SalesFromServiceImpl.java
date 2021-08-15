package com.warehouse.service.impl;

import com.warehouse.dao.SalesFromInterface;
import com.warehouse.pojo.Sales;
import com.warehouse.pojo.Salesback;
import com.warehouse.service.SalesFromService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("SalesFromService")
public class SalesFromServiceImpl implements SalesFromService {

    @Autowired
    private SalesFromInterface dao;

    public List<Sales> getSalesList(int page, int limit, String startTime, String endTime, Integer id) {
        return dao.getSalesList(page,limit,startTime,endTime,id);
    }

    public int getSalesListCount(String startTime,String endTime,Integer id) {
        return dao.getSalesListCount(startTime,endTime,id);
    }



    public List<Salesback> getSaleBackList(int page, int limit, String startTime, String endTime, Integer id) {
        return dao.getSaleBackList(page,limit,startTime,endTime,id);
    }

    public int getSaleBackListCount(String startTime,String endTime,Integer id) {
        return dao.getSaleBackListCount(startTime,endTime,id);
    }



    public int addApprove(String approveName,int id) {
        return dao.addApprove(approveName,id);
    }

    public int updateSalesState(int id) {
        return dao.updateSalesState(id);
    }

    public int addApproveB(String approveName, int id) {
        return dao.addApproveB(approveName,id);
    }

    public int updateSalesBackState(int id) {
        return dao.updateSalesBackState(id);
    }
}
