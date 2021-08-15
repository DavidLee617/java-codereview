package com.warehouse.service;

import com.warehouse.pojo.Outport;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OutportService {
    public int getOutportState();
    List<Outport> getOutportList(@Param("startTime") String startTime
            , @Param("endTime") String endTime
            , @Param("ID") Integer ID);

    List<Outport> getApprovedOutportList(@Param("startTime") String startTime
            , @Param("endTime") String endTime
            , @Param("ID") Integer ID);

    boolean outport(@Param("providerName") String providerName,
                    @Param("goodsName") String goodsName,
                    @Param("goodsSize") String goodsSize,
                    @Param("number") Integer number,
                    @Param("outputPrice") Double outputPrice,
                    @Param("payType") String payType,
                    @Param("operateName") String operateName,
                    @Param("reason") String reason);

    boolean approveOut(@Param("id") Integer id, @Param("approveName") String approveName);

    boolean outportFromWarehouse(@Param("id") Integer id, @Param("warehouseName") String warehouseName);
}
