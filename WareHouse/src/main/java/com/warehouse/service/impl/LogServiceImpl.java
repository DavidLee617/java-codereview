package com.warehouse.service.impl;

import com.warehouse.dao.LogDao;
import com.warehouse.pojo.Log;
import com.warehouse.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("LogService")
public class LogServiceImpl implements LogService {
    @Autowired
    private LogDao logDao;

    @Override
    public int getLogCount() {
        return logDao.getLogCount();
    }

    @Override
    public List<Log> getLogList(int page, int limit, String startTime, String endTime, String name) {
        return logDao.getLogList(page, limit , startTime, endTime, name);
    }

    @Override
    public int selectCount(String startTime, String endTime, String name) {
        return logDao.selectCount(startTime, endTime, name);
    }

    @Override
    public int insertLog(String name, String ip, String time) {
        return logDao.insertLog(name, ip, time);
    }


}
