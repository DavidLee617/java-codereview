package com.warehouse.service.impl;

import com.warehouse.dao.WareDao;
import com.warehouse.pojo.Ware;
import com.warehouse.service.WareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service("wareService")
public class WareServiceImpl implements WareService {
    @Autowired
    private WareDao wareDao;
//全查仓库
    @Override
    public List<Ware> getWareList(int page, int limit) {
        return wareDao.getWareList(page,limit);
    }
//查看仓库数量数据
    @Override
    public int getWarecount() {
        return wareDao.getWarecount();
    }
    //添加
    @Override
    public boolean addWL(int count,String wareName) {
        wareDao.addWL(wareName);
        int a = wareDao.getIdByName(wareName);
        String locationName;
        for (int i = 1;i <= count; i++) {
            locationName = "库位" + i;
            wareDao.addWARE(a,locationName);
        }
        return true;
    }



    @Override
    public Ware selectById(int id) {
        return wareDao.selectById(id);
    }
}
