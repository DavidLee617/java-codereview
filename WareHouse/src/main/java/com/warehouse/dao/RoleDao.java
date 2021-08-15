package com.warehouse.dao;

import com.warehouse.pojo.Role;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoleDao {
    public int getRoleState();
    public List<Role> getRoleList();
    public Role getRoleById(String name);
    public int updateRole(Role role);
    public int addRole(Role role);
    public int addPermission(@Param("rid")Integer rid,@Param("pid")Integer pid);
    public int deleteRole(@Param("id")Integer id);
}
