package com.warehouse.service;

import com.warehouse.pojo.Log;

import java.util.List;

public interface LogService {

    public int getLogCount();

    public List<Log> getLogList(int page, int limit, String startTime, String endTime, String name);

    public int selectCount(String startTime, String endTime, String name);
    public int insertLog(String name,String ip,String time);
}
