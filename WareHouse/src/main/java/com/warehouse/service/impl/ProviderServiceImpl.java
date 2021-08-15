package com.warehouse.service.impl;

import com.warehouse.dao.ProviderDao;
import com.warehouse.pojo.Provider;
import com.warehouse.service.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service("providerService")
public class ProviderServiceImpl implements ProviderService {

    @Autowired
    private ProviderDao providerDao;

    @Override
    public int getProviderState() {
        return providerDao.getProviderState();
    }

    //全查供应商
    @Override
    public List<Provider> getProviderList(int page, int limit) {
        return providerDao.getProviderList(page, limit);
    }

    //查看供应商数量
    @Override
    public int getProvidercount() {
        return providerDao.getProvidercount();
    }

    @Override
    public int updateProvider(int id, String providername, String zip, String address, String tel, String contactname, String contacttel, String bank, String account, String email, int state) {
        return providerDao.updateProvider(id,providername,zip,address,tel,contactname,contacttel,bank,account,email,state);
    }

    //修改供应商

    //添加供应商
    @Override
    public boolean addProvider(String providername, String zip, String address, String tel, String contactname, String contacttel, String bank, String account, String email, Integer state) {
        return providerDao.addProvider(providername, zip, address, tel, contactname, contacttel, bank, account, email, state) == 1;
    }

    @Override
    public Provider selectById(String providername) {
        return providerDao.selectById(providername);
    }
    //模糊查询
}
