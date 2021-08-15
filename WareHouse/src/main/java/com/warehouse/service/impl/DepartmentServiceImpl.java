package com.warehouse.service.impl;

import com.warehouse.dao.DepartmentDao;
import com.warehouse.pojo.Department;
import com.warehouse.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service("DepartmentService")
public class DepartmentServiceImpl implements DepartmentService {
    @Autowired
    private DepartmentDao departmentDao;
    @Override
    public List<Department> getDepartmentList() {
        return departmentDao.getDepartmentList();
    }

    @Override
    public int addDepartment(Department department) {
        return departmentDao.addDepartment(department);
    }

    @Override
    public int updateDepartment(Department department) {
        return departmentDao.updateDepartment(department);
    }

    @Override
    public int deleteDepartment(Integer id) {
        return departmentDao.deleteDepartment(id);
    }
}
