package com.warehouse.service.impl;

import com.warehouse.dao.PermissionDao;
import com.warehouse.pojo.Permission;
import com.warehouse.service.PermissionService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service("PermissionService")
public class PermissionServiceImpl implements PermissionService {
    @Autowired
    private PermissionDao permissionDao;

    @Override
    public int getPermissionState() {
        return permissionDao.getPermissionState();
    }

    @Override
    public List<Permission> getPermissionList(@Param("page")Integer page, @Param("limit")Integer limit) {
        return permissionDao.getPermissionList(page,limit);
    }

    @Override
    public int getPermissionCount() {
        return permissionDao.getPermissionCount();
    }

    @Override
    public List<Permission> getPermissionListByRid(Integer rid) {
        return permissionDao.getPermissionListByRid(rid);
    }
}
