package com.warehouse.dao;

import com.warehouse.pojo.Notice;

import java.util.List;

public interface NoticeDao {
    /*查询公告表所有数量*/
    public int getNoticeCount();

    /*查询公告表所有*/
    public List<Notice> selectNoticeList(int page, int limit, String startTime, String endTime, String title);

    /*查询所有公告数量*/
    public int selectNoticeCount(String startTime, String endTime, String title);

    /*添加*/
    public int addNotice(Integer id, String title, String context, String time, String operateName);
    /*修改*/
    public int updatenotice(Integer id, String title, String context, String time, String operateName);
    /*删除*/
    public int deleteNotice(Integer id);
    /*根据id查询*/
    public List<Notice> selectNoticeId(Integer id);

}
