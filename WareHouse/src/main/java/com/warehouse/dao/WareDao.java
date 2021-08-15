package com.warehouse.dao;

import com.warehouse.pojo.Ware;

import java.util.List;

public interface WareDao {
//    全查仓库
    public List<Ware> getWareList(int page, int limit);
//查看仓库数量
    public int getWarecount();

//    模糊查询
    Ware selectById(int id);
//添加
    int addWL(String wareName);
    int addWARE(int wareId, String locationName);
    int getIdByName(String name);

}
