package com.warehouse.dao;

import com.warehouse.pojo.*;
import com.warehouse.pojo.Package;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GoodsDao {
//    全查商品
    public List<Goods> getGoodsList(int page, int limit);
//查看商品数量
    public int getGoodscount();
//修改商品
    int updateGoods(int id, String goodsName, String producePlace, String goodsType, String size, String type, String productCode, String promitCode, String description, double inportprice, double salesprice, String providername, int state, int number, String goodsImg, String wareName, String locationName);
//    添加商品
    public Integer addGoods(@Param("goodsName") String goodsName,
                            @Param("producePlace") String producePlace,
                            @Param("goodsType") String goodsType,
                            @Param("size") String size,
                            @Param("packageId") Integer packageId,
                            String type,
                            @Param("productCode") String productCode,
                            @Param("promitCode") String promitCode,
                            @Param("description") String description,
                            @Param("inportprice") double inportprice,
                            @Param("salesprice") double salesprice,
                            @Param("providerId") Integer providerId,
                            String providername,
                            @Param("state") Integer state,
                            @Param("number") Integer number,
                            @Param("goodsImg") String goodsImg,
                            Integer wareId,
                            @Param("wareName") String wareName,
                            Integer locationId,
                            @Param("locationName") String locationName);
//    删除商品
    public int deleteGoods(int id);

//    模糊查询
    public Goods selectById(String goodsName);

    //    查询仓库
    List<Ware> selectWare();
    //    查询库位
    List<Location> selectLocation();

//    查询商品类型
    List<Package> selectPackage();

//    查询供应商名称
    List<Provider> selectProvider();

    public int addGoodsImg(String goodImg);
    public Goods getGoodsId(String goodImg);

    public List<Goods> getGoodsNumber(int page,int limit);

    public int getGoodsState();
}
