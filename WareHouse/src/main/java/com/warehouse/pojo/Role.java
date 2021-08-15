package com.warehouse.pojo;

import java.util.List;

/**
 * 角色表
 */
public class Role {
    private Integer id;
    private String name;
    private Integer state;
    private String remark;
    private List<Permission> permission;

    public Role() {
    }

    public Role(Integer id, String name, Integer state, String remark) {
        this.id = id;
        this.name = name;
        this.state = state;
        this.remark = remark;
    }

    public List<Permission> getPermission() {
        return permission;
    }

    public void setPermission(List<Permission> permission) {
        this.permission = permission;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

}
