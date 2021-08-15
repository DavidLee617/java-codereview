package com.warehouse.service;

import com.warehouse.pojo.Department;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DepartmentService {
    public List<Department> getDepartmentList();//查询全部部门
    public int addDepartment(Department department);
    public int updateDepartment(Department department);
    public int deleteDepartment(@Param("id") Integer id);
}
