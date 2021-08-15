package com.warehouse.service;

import com.warehouse.pojo.Inport;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface InportService {
    public int getInportState();
    List<Inport> getInportList(@Param("startTime") String startTime
            , @Param("endTime") String endTime
            , @Param("ID") Integer ID);

    List<Inport> getApprovedInportList(@Param("startTime") String startTime
            , @Param("endTime") String endTime
            , @Param("ID") Integer ID);

    boolean getProviderIdByName(@Param("name") String providerName);

    boolean getGoodsIdByName(@Param("name") String goodsName);


    boolean inport(@Param("providerName") String providerName,
                   @Param("goodsName") String goodsName,
                   @Param("goodsSize") String goodsSize,
                   @Param("goodsCount") Integer goodsCount,
                   @Param("inportPrice") Double inportPrice,
                   @Param("payType") String payType,
                   @Param("operateName") String operateName,
                   @Param("remark") String remark,
                   @Param("reason") String reason);

    List<String> getProviderNames(@Param("key") String key);

    List<String> getGoodsName(@Param("key") String key);

    List<String> getGoodsSize(@Param("goodsName") String goodsName);

    Inport getInportById(@Param("id") Integer id);

    boolean updateInport(@Param("providerName") String providerName,
                         @Param("goodsName") String goodsName,
                         @Param("goodsSize") String goodsSize,
                         @Param("goodsCount") Integer goodsCount,
                         @Param("inportPrice") Double inportPrice,
                         @Param("payType") String payType,
                         @Param("remark") String remark,
                         @Param("id") Integer id);

    boolean approveAdd(@Param("id") Integer id, @Param("approveName") String approveName);

    boolean inportIntoWarehouse(@Param("id") Integer id, @Param("warehouseName") String warehouseName);
}
