package com.warehouse.dao;

import com.warehouse.pojo.Log;

import java.util.List;

public interface LogDao {
    /*查询日志总条数*/
    public int getLogCount();
    /*查询所有日志*/
    public List<Log> getLogList(int page, int limit, String startTime, String endTime, String name);
    /*根据Id模糊查询*/
    public int selectCount(String startTime, String endTime, String name);

    public int insertLog(String name,String ip,String time);
}
