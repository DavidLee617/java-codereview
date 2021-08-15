package com.warehouse.service.impl;

import com.warehouse.dao.InportDao;
import com.warehouse.pojo.Inport;
import com.warehouse.pojo.Provider;
import com.warehouse.service.InportService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @ Author
 */
@Service("inportService")
public class InportServiceImpl implements InportService {
    @Autowired
    private InportDao inportDao;

    @Override
    public int getInportState() {
        return inportDao.getInportState();
    }

    @Override
    public List<Inport> getInportList(String startTime
           ,String endTime
            ,Integer ID) {
        return inportDao.getInportList(startTime,endTime,ID);
    }

    @Override
    public List<Inport> getApprovedInportList(String startTime
            ,String endTime
            ,Integer ID) {
        return inportDao.getApprovedInportList(startTime,endTime,ID);
    }

    @Override
    public boolean getProviderIdByName(String providerName) {
        return inportDao.getProviderIdByName(providerName) != null;
    }

    @Override
    public boolean getGoodsIdByName(String goodsName) {
        return inportDao.getGoodsIdByName(goodsName) != null;
    }

    @Override
    public boolean inport(String providerName, String goodsName,
                            String goodsSize, Integer goodsCount,
                            Double inportPrice, String payType,
                            String operateName,
                            String remark,String reason) {
        if (inportDao.getProviderIdByName(providerName) != null
                && inportDao.getGoodsIdByName(goodsName) != null){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            提交申请时间
            String approveTime = sdf.format(new Date());
            inportDao.inport(providerName,goodsName,goodsSize,goodsCount,inportPrice,payType,
                    approveTime,operateName,remark,reason);
            return true;
        }
        return false;

    }

    @Override
    public List<String> getProviderNames(String key) {
        List<String> list = new ArrayList<>();
        if (!(key == null) && !"".equals(key)){
            list =  inportDao.getProviderNames(key);
        }
        return list;
    }

    @Override
    public List<String> getGoodsName(String key) {
        List<String> list = new ArrayList<>();
        if (!(key == null) && !"".equals(key)){
            list =  inportDao.getGoodsName(key);
        }
        return list;
    }

    @Override
    public List<String> getGoodsSize(String goodsName) {
        List<String> list = new ArrayList<>();
        if (!(goodsName == null) && !"".equals(goodsName)){
            list =  inportDao.getGoodsSize(goodsName);
        }
        return list;
    }

    @Override
    public Inport getInportById(Integer id) {
        return inportDao.getInportById(id);
    }

    @Override
    public boolean updateInport(String providerName, String goodsName,
                                String goodsSize, Integer goodsCount,
                                Double inportPrice, String payType,
                                String remark,Integer id) {

        if (inportDao.getProviderIdByName(providerName) != null
                && inportDao.getGoodsIdByName(goodsName) != null){

            inportDao.updateInport(providerName,goodsName,goodsSize,goodsCount,inportPrice,payType,remark,id);
            return true;
        }else {
            return false;
        }

    }

    @Override
    public boolean approveAdd(Integer id,String approveName) {
        if (inportDao.getStatebyId(id) == 0){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String approveSuccessTime = sdf.format(new Date());
            return inportDao.approveAdd(id) == 1
                    && inportDao.updataApprove(id,approveName,approveSuccessTime) == 1;
        }
        return false;

    }

    @Override
    public boolean inportIntoWarehouse(Integer id,String warehouseName) {
        if (inportDao.getStatebyId(id) == 1){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String inportTime = sdf.format(new Date());

            inportDao.updateWarehouseName(warehouseName,id,inportTime);
            int goodsId = inportDao.getGoodsIdFromInport(id);
//        要入库商品的数量
            int inportNum = inportDao.getNumFromInport(id);
            int num = inportDao.getNumFromGoods(goodsId);
//        改变仓库里商品的数量，入库
            int newNum = num + inportNum;
            return inportDao.inportIntoWarehouse(goodsId,newNum) == 1;
        }else {
            return false;
        }
    }
}
