package com.warehouse.dao;

import com.warehouse.pojo.Outport;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OutportDao {
    public int getOutportState();

    List<Outport> getOutportList(@Param("startTime") String startTime
            , @Param("endTime") String endTime
            , @Param("ID") Integer ID);

    List<Outport> getApprovedOutportList(@Param("startTime") String startTime
            , @Param("endTime") String endTime
            , @Param("ID") Integer ID);

    Integer outport(@Param("providerName") String providerName,
                    @Param("goodsName") String goodsName,
                    @Param("goodsSize") String goodsSize,
                    @Param("number") Integer number,
                    @Param("outputPrice") Double outputPrice,
                    @Param("payType") String payType,
                    @Param("approveTime") String approveTime,
                    @Param("operateName") String operateName,
                    @Param("reason") String reason);

    Integer approveOut(@Param("id") Integer id);

    Integer getStatebyId(@Param("id") Integer id);

    Integer updataApprove(@Param("outportId") Integer inportId, @Param("approveName") String approveName
            , @Param("approveSuccessTime") String approveSuccessTime);

    Integer getNumFromGoods(@Param("goodsId") Integer goodsId);

    Integer getNumFromOutport(@Param("id") Integer id);

    Integer getGoodsIdFromOutport(@Param("id") Integer id);

    Integer updateWarehouseName(@Param("warehouseName") String warehouseName, @Param("id") Integer id
            , @Param("outportTime") String outportTime);

    Integer outportFromWarehouse(@Param("id") Integer id, @Param("num") Integer num);

}
