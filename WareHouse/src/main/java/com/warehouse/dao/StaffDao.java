package com.warehouse.dao;

import com.warehouse.pojo.Role;
import com.warehouse.pojo.Staff;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StaffDao {
    public Staff getStaff(@Param("jobNumber")String jobNumber);//认证员工账号密码登录
    public Staff getStaffByTel(@Param("tel")String tel);//认证员工免密登录
    public Staff getStaffById(@Param("id")Integer id);
    public List<Staff> getStaffListByName(@Param("jobNumber")String jobNumber);//授权
    public List<Staff> getStaffList(@Param("page")Integer page,@Param("limit")Integer limit,@Param("jobNumber")String jobNumber,String startTime,String endTime);//查全部员工信息
    public int addStaff(Staff staff);
    public int updateStaff(Staff staff);
    public int updateStaffIndex(Staff staff);
    public int deleteStaff(@Param("id")Integer id);
    public List<Role> getRoleByName(@Param("name")String name);
    public int getStaffCount(@Param("jobNumber")String jobNumber,String startTime,String endTime);
    public int updatePwd(@Param("id")Integer id,@Param("password")String password);
}
