package com.warehouse.service.impl;

import com.warehouse.dao.RoleDao;
import com.warehouse.pojo.Role;
import com.warehouse.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service("RoleService")
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleDao roleDao;

    @Override
    public int getRoleState() {
        return roleDao.getRoleState();
    }

    @Override
    public List<Role> getRoleList() {
        return roleDao.getRoleList();
    }

    @Override
    public int updateRole(Role role) {
        return roleDao.updateRole(role);
    }

    @Override
    public int addRole(Role role) {
        return roleDao.addRole(role);
    }

    @Override
    public int addPermission(Integer rid, Integer pid) {
        return roleDao.addPermission(rid, pid);
    }

    @Override
    public int deleteRole(Integer id) {
        return roleDao.deleteRole(id);
    }

}
