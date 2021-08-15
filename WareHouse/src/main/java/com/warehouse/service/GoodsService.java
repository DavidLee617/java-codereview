package com.warehouse.service;

import com.warehouse.pojo.Goods;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface GoodsService {
    public List<Goods> getGoodsNumber(int page,int limit);
    public int getGoodsState();

    public int addGoodsImg(String goodImg);
    public Goods getGoodsId(String goodImg);
//    全查商品
    public List<Goods> getGoodslist(int page, int limit);
//查看商品数量
    public int getGoodsCount();
//修改商品
int updateGoods(int id, String goodsName, String producePlace, String goodsType, String size, String type, String productCode, String promitCode, String description, double inportprice, double salesprice, String providername, int state, int number, String goodsImg, String wareName, String locationName);
//    添加商品
    boolean addGoods(@Param("goodsName") String goodsName,
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

//    删除客户
    int deleteGoods(int id);

//    模糊查询
    Goods selectById(String goodsName);

    //    查询仓库
    Map<String,List> selectWare();

    //    查询商品类型
    Map<String,List> selectPackage();

    //    查询供应商名称
    Map<String,List> selectProvider();

}
