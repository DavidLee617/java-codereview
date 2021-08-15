package com.warehouse.pojo;

import java.util.List;

/**
 * 员工表
 */
public class Staff {
    private Integer id;
    private String name;
    private String jobNumber;
    private String password;
    private String idCard;
    private String tel;
    private Integer sex;
    private Integer age;
    private String address;
    private String entryTime;
    private Integer state;
    private Integer dapatmentId;
    private Integer roleId;
    private String remark;
    private String pic;
    private List<Role> roleList;
    private Role role;
    private Permission permission;
    private Department department;

    public Staff() {
    }

    public Staff(Integer id, String name, String jobNumber, String password, String idCard, String tel, Integer sex, Integer age, String address, String entryTime, Integer state, Integer dapatmentId, Integer roleId, String remark, String pic) {
        this.id = id;
        this.name = name;
        this.jobNumber = jobNumber;
        this.password = password;
        this.idCard = idCard;
        this.tel = tel;
        this.sex = sex;
        this.age = age;
        this.address = address;
        this.entryTime = entryTime;
        this.state = state;
        this.dapatmentId = dapatmentId;
        this.roleId = roleId;
        this.remark = remark;
        this.pic = pic;
    }

    public List<Role> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<Role> roleList) {
        this.roleList = roleList;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
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

    public String getJobNumber() {
        return jobNumber;
    }

    public void setJobNumber(String jobNumber) {
        this.jobNumber = jobNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(String entryTime) {
        this.entryTime = entryTime;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getDapatmentId() {
        return dapatmentId;
    }

    public void setDapatmentId(Integer dapatmentId) {
        this.dapatmentId = dapatmentId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }
}
