package com.warehouse.service;

import com.warehouse.pojo.Notice;

import java.util.List;

public interface NoticeService {
    public int getNoticeCount();

    public List<Notice> selectNoticeList(int page, int limit, String startTime, String endTime, String title);

    public int selectNoticeCount(String startTime, String endTime, String title);

    public int addNotice(Integer id, String title, String context, String time, String operateName);
    public int updatenotice(Integer id, String title, String context, String time, String operateName);
    public int deleteNotice(Integer id);
    public List<Notice> selectNoticeId(Integer id);
}
