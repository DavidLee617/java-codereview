package com.warehouse.pojo;

/**
 * 权限表
 */
public class Permission {
    private Integer id;
    private String name;
    private String p_name;
    private Integer state;
    private String remark;

    public Permission() {
    }

    public Permission(Integer id, String name, String p_name, Integer state, String remark) {
        this.id = id;
        this.name = name;
        this.p_name = p_name;
        this.state = state;
        this.remark = remark;
    }

    public String getP_name() {
        return p_name;
    }

    public void setP_name(String p_name) {
        this.p_name = p_name;
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
