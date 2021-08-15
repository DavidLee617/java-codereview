package com.warehouse.pojo;

public class Approve {
    private Integer id;
    private String approveName;
    private String approveSuccessTime;
    private Integer inportId;
    private Integer outportId;
    private Integer salesId;
    private Integer salsebackId;

    public Approve() {
    }

    public Approve(Integer id, String approveName, String approveSuccessTime, Integer inportId, Integer outportId, Integer salesId, Integer salsebackId) {
        this.id = id;
        this.approveName = approveName;
        this.approveSuccessTime = approveSuccessTime;
        this.inportId = inportId;
        this.outportId = outportId;
        this.salesId = salesId;
        this.salsebackId = salsebackId;
    }

    public String getApproveSuccessTime() {
        return approveSuccessTime;
    }

    public void setApproveSuccessTime(String approveSuccessTime) {
        this.approveSuccessTime = approveSuccessTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getApproveName() {
        return approveName;
    }

    public void setApproveName(String approveName) {
        this.approveName = approveName;
    }


    public Integer getInportId() {
        return inportId;
    }

    public void setInportId(Integer inportId) {
        this.inportId = inportId;
    }

    public Integer getOutportId() {
        return outportId;
    }

    public void setOutportId(Integer outportId) {
        this.outportId = outportId;
    }

    public Integer getSalesId() {
        return salesId;
    }

    public void setSalesId(Integer salesId) {
        this.salesId = salesId;
    }

    public Integer getSalsebackId() {
        return salsebackId;
    }

    public void setSalsebackId(Integer salsebackId) {
        this.salsebackId = salsebackId;
    }
}
