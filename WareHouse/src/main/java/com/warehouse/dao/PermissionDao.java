package com.warehouse.dao;

import com.warehouse.pojo.Permission;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PermissionDao {
   public int getPermissionState();

   public List<Permission> getPermissionList(@Param("page")Integer page, @Param("limit")Integer limit);

   public int getPermissionCount();

   public List<Permission> getPermissionListByRid(@Param("rid")Integer rid);
}
