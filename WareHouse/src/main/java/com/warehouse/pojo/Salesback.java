package com.warehouse.pojo;

public class Salesback {
    private Integer id;
    private Integer customerId;
    private String payType;
    private String salesbackTime;
    private String operateName;
    private Integer number;
    private Integer goodsId;
    private String reason;
    private String warehouseName;
    private String approveTime;
    private Integer state;
    private Goods goods;
    private Customar customar;
    private Approve approve;

    public Salesback(Integer id, Integer customerId, String payType, String salesbackTime, String operateName, Integer number, Integer goodsId, String reason, String warehouseName, String approveTime, Integer state, Goods goods, Customar customar, Approve approve) {
        this.id = id;
        this.customerId = customerId;
        this.payType = payType;
        this.salesbackTime = salesbackTime;
        this.operateName = operateName;
        this.number = number;
        this.goodsId = goodsId;
        this.reason = reason;
        this.warehouseName = warehouseName;
        this.approveTime = approveTime;
        this.state = state;
        this.goods = goods;
        this.customar = customar;
        this.approve = approve;
    }

    public Salesback() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getSalesbackTime() {
        return salesbackTime;
    }

    public void setSalesbackTime(String salesbackTime) {
        this.salesbackTime = salesbackTime;
    }

    public String getOperateName() {
        return operateName;
    }

    public void setOperateName(String operateName) {
        this.operateName = operateName;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getApproveTime() {
        return approveTime;
    }

    public void setApproveTime(String approveTime) {
        this.approveTime = approveTime;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Goods getGoods() {
        return goods;
    }

    public void setGoods(Goods goods) {
        this.goods = goods;
    }

    public Customar getCustomar() {
        return customar;
    }

    public void setCustomar(Customar customar) {
        this.customar = customar;
    }

    public Approve getApprove() {
        return approve;
    }

    public void setApprove(Approve approve) {
        this.approve = approve;
    }
}
