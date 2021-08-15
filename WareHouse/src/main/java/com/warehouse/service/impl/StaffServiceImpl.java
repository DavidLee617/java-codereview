package com.warehouse.service.impl;

import com.warehouse.dao.StaffDao;
import com.warehouse.pojo.Role;
import com.warehouse.pojo.Staff;
import com.warehouse.service.StaffService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service("StaffService")
public class StaffServiceImpl implements StaffService {
    @Autowired
    private StaffDao staffDao;

    @Override
    public Staff getStaff(String jobNumber) {
        return staffDao.getStaff(jobNumber);
    }

    @Override
    public Staff getStaffByTel(String tel) {
        return staffDao.getStaffByTel(tel);
    }

    @Override
    public Staff getStaffById(Integer id) {
        return staffDao.getStaffById(id);
    }

    @Override
    public List<Staff> getStaffListByName(String jobNumber) {
        return staffDao.getStaffListByName(jobNumber);
    }

    @Override
    public List<Staff> getStaffList(@Param("page")Integer page,@Param("limit")Integer limit,@Param("jobNumber")String jobNumber,String startTime,String endTime) {
        return staffDao.getStaffList(page,limit,jobNumber,startTime,endTime);
    }

    @Override
    public int addStaff(Staff staff) {
        return staffDao.addStaff(staff);
    }

    @Override
    public int updateStaff(Staff staff) {
        return staffDao.updateStaff(staff);
    }

    @Override
    public int updateStaffIndex(Staff staff) {
        return staffDao.updateStaffIndex(staff);
    }

    @Override
    public int deleteStaff(Integer id) {
        return staffDao.deleteStaff(id);
    }

    @Override
    public List<Role> getRoleByName(String name) {
        return staffDao.getRoleByName(name);
    }

    @Override
    public int getStaffCount(@Param("jobNumber")String jobNumber,String startTime,String endTime) {
        return staffDao.getStaffCount(jobNumber, startTime, endTime);
    }

    @Override
    public int updatePwd(Integer id, String password) {
        return staffDao.updatePwd(id, password);
    }
}
