package com.warehouse.service.impl;

import com.warehouse.dao.InportDao;
import com.warehouse.dao.OutportDao;
import com.warehouse.pojo.Outport;
import com.warehouse.service.OutportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @ Author
 */
@Service("outportService")
public class OutportServiceImpl implements OutportService {
    @Autowired
    private OutportDao outportDao;

    @Autowired
    private InportDao inportDao;

    @Override
    public int getOutportState() {
        return outportDao.getOutportState();
    }

    @Override
    public List<Outport> getOutportList(String startTime
            ,String endTime
            ,Integer ID) {
        return outportDao.getOutportList(startTime,endTime,ID);
    }

    @Override
    public List<Outport> getApprovedOutportList(String startTime
            ,String endTime
            ,Integer ID) {
        return outportDao.getApprovedOutportList(startTime,endTime,ID);
    }

    @Override
    public boolean outport(String providerName, String goodsName,
                           String goodsSize, Integer number,
                           Double outputPrice, String payType,
                           String operateName, String reason) {

        if (inportDao.getProviderIdByName(providerName) != null
                && inportDao.getGoodsIdByName(goodsName) != null){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String approveTime = sdf.format(new Date());
            outportDao.outport(providerName,goodsName,goodsSize,number,
                    outputPrice,payType,approveTime,operateName,reason);
            return true;
        }
        return false;
    }

    @Override
    public boolean approveOut(Integer id, String approveName) {
        if (outportDao.getStatebyId(id) == 0){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String approveSuccessTime = sdf.format(new Date());
            return outportDao.approveOut(id) == 1
                    && outportDao.updataApprove(id,approveName,approveSuccessTime) == 1;
        }
        return false;
    }

    @Override
    public boolean outportFromWarehouse(Integer id,String warehouseName) {
        if (outportDao.getStatebyId(id) == 1){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String inportTime = sdf.format(new Date());

            outportDao.updateWarehouseName(warehouseName,id,inportTime);
            int goodsId = outportDao.getGoodsIdFromOutport(id);
//        要入库商品的数量
            int inportNum = outportDao.getNumFromOutport(id);
            int num = outportDao.getNumFromGoods(goodsId);
//        改变仓库里商品的数量，入库
            int newNum = num - inportNum;
            return outportDao.outportFromWarehouse(goodsId,newNum) == 1;
        }else {
            return false;
        }
    }
}
