package com.warehouse.dao;


import com.warehouse.pojo.Inport;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface InportDao {
    public int getInportState();

    List<Inport> getInportList(@Param("startTime") String startTime
            , @Param("endTime") String endTime
            , @Param("ID") Integer ID);

    List<Inport> getApprovedInportList(@Param("startTime") String startTime
            , @Param("endTime") String endTime
            , @Param("ID") Integer ID);

    Integer getProviderIdByName(@Param("name") String providerName);

    List<Integer> getGoodsIdByName(@Param("name") String goodsName);

    Integer inport(@Param("providerName") String providerName,
                   @Param("goodsName") String goodsName,
                   @Param("goodsSize") String goodsSize,
                   @Param("goodsCount") Integer goodsCount,
                   @Param("inportPrice") Double inportPrice,
                   @Param("payType") String payType,
                   @Param("approveTime") String approveTime,
                   @Param("operateName") String operateName,
                   @Param("remark") String remark,
                   @Param("reason") String reason);

    List<String> getProviderNames(@Param("key") String key);

    List<String> getGoodsName(@Param("key") String key);

    List<String> getGoodsSize(@Param("goodsName") String goodsName);

    Inport getInportById(@Param("id") Integer id);

    Integer updateInport(@Param("providerName") String providerName,
                         @Param("goodsName") String goodsName,
                         @Param("goodsSize") String goodsSize,
                         @Param("goodsCount") Integer goodsCount,
                         @Param("inportPrice") Double inportPrice,
                         @Param("payType") String payType,
                         @Param("remark") String remark,
                         @Param("id") Integer id);

    Integer updataApprove(@Param("inportId") Integer inportId, @Param("approveName") String approveName
            , @Param("approveSuccessTime") String approveSuccessTime);

    Integer approveAdd(@Param("id") Integer id);

    Integer getNumFromGoods(@Param("goodsId") Integer goodsId);

    Integer getNumFromInport(@Param("id") Integer id);

    Integer getGoodsIdFromInport(@Param("id") Integer id);

    Integer updateWarehouseName(@Param("warehouseName") String warehouseName, @Param("id") Integer id
            , @Param("inportTime") String inportTime);

    Integer inportIntoWarehouse(@Param("id") Integer id, @Param("num") Integer num);

    Integer getStatebyId(@Param("id") Integer id);
}
