package com.warehouse.service.impl;

import com.warehouse.dao.SalesbackDao;
import com.warehouse.service.SalesbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("SalesbackService")
public class SalesbackServiceImpl implements SalesbackService {
    @Autowired
    private SalesbackDao salesbackDao;


    @Override
    public int getSalesbackState() {
        return salesbackDao.getSalesbackState();
    }
}
