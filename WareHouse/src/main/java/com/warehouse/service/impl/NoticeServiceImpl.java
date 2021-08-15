package com.warehouse.service.impl;

import com.warehouse.dao.NoticeDao;
import com.warehouse.pojo.Notice;
import com.warehouse.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("NoticeService")
public class NoticeServiceImpl implements NoticeService {
    @Autowired
    private NoticeDao noticeDao;
    @Override
    public int getNoticeCount() {
        return noticeDao.getNoticeCount();
    }
    @Override
    public List<Notice> selectNoticeList(int page, int limit, String startTime, String endTime, String title) {
        return noticeDao.selectNoticeList(page, limit, startTime, endTime, title);
    }
    @Override
    public int selectNoticeCount(String startTime, String endTime, String title) {
        return noticeDao.selectNoticeCount(startTime, endTime, title);
    }
    @Override
    public int addNotice(Integer id, String title, String context, String time, String operateName) {
        return noticeDao.addNotice(id, title, context, time, operateName);
    }
    @Override
    public int updatenotice(Integer id, String title, String context, String time, String operateName) {
        return noticeDao.updatenotice(id, title, context, time, operateName);
    }
    @Override
    public int deleteNotice(Integer id) {
        return noticeDao.deleteNotice(id);
    }
    @Override
    public List<Notice> selectNoticeId(Integer id) {
        return noticeDao.selectNoticeId(id);
    }
}
