package com.warehouse.service;

import com.warehouse.pojo.Ware;

import java.util.List;

public interface WareService {
//    全看仓库
    public List<Ware> getWareList(int page, int limit);
//查看仓库数量
    public int getWarecount();

//    添加
    boolean addWL(int count, String wareName);

    //    模糊查询
    Ware selectById(int id);
}
