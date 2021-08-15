package com.warehouse.service;

import com.warehouse.pojo.Sales;
import com.warehouse.pojo.Salesback;

import java.util.List;

public interface SalesFromService {
    public List<Sales> getSalesList(int page, int limit, String startTime, String endTime, Integer id);
    public int getSalesListCount(String startTime, String endTime, Integer id);

    public List<Salesback> getSaleBackList(int page, int limit, String startTime, String endTime, Integer id);
    public int getSaleBackListCount(String startTime, String endTime, Integer id);

    public int addApprove(String approveName, int id);
    public int updateSalesState(int id);

    public int addApproveB(String approveName, int id);
    public int updateSalesBackState(int id);

}
