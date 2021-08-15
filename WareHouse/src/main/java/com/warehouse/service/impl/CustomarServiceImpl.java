package com.warehouse.service.impl;

import com.warehouse.dao.CustomarDao;
import com.warehouse.pojo.Customar;
import com.warehouse.service.CustomarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service("CustomarService")
public class CustomarServiceImpl implements CustomarService {
    @Autowired
    private CustomarDao customarDao;

    @Override
    public int getcustomarState() {
        return customarDao.getcustomarState();
    }

    //全查客户
    @Override
    public List<Customar> getCustomarList(int page, int limit) {
        return customarDao.getCustomarList(page,limit);
    }
    //查询客户数量数据
    public int getCustomarcount() {
        return customarDao.getCustomarcount();
    }
    //修改
    @Override
    public int updateCustomer(int id, String customerName, String zip, String tel, String address, String contactName, String contactTel, String bank, String account, String email, int state) {
        return customarDao.updateCustomar(id,customerName,zip,tel,address,contactName,contactTel,bank,account,email,state);
    }


//添加客户
    @Override
    public boolean addCustomar(String customerName, String zip, String tel, String address, String contactName, String contactTel, String email, String bank, String account, Integer state) {
        return customarDao.addCustomar(customerName,zip,tel,address,contactName,contactTel,email,bank,account,state) == 1;
    }
//删除
    @Override
    public int deleteCustomar(int id) {
        return customarDao.deleteCustomar(id);
    }
//模糊
    @Override
    public Customar selectById(String customerName) {
        return customarDao.selectById(customerName);
    }
}
