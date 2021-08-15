package com.warehouse.pojo;

public class Outport {
    private Integer id;
    private Integer providerId;
    private String payType;
    private String outputTime;
    private String operateName;
    private Integer number;
    private Integer goodsId;
    private String reason;
    private String approveTime;
    private String warehouseName;
    private Integer state;
    private Double outputPrice;
    private Goods goods;
    private Provider provider;
    private Approve approve;

    public Outport() {
    }

    public Outport(Integer id, Integer providerId, String payType, String outputTime, String operateName, Integer number, Integer goodsId, String reason, String approveTime, String warehouseName, Integer state) {
        this.id = id;
        this.providerId = providerId;
        this.payType = payType;
        this.outputTime = outputTime;
        this.operateName = operateName;
        this.number = number;
        this.goodsId = goodsId;
        this.reason = reason;
        this.approveTime = approveTime;
        this.warehouseName = warehouseName;
        this.state = state;
    }

    public Double getOutputPrice() {
        return outputPrice;
    }

    public void setOutputPrice(Double outputPrice) {
        this.outputPrice = outputPrice;
    }

    public Approve getApprove() {
        return approve;
    }

    public void setApprove(Approve approve) {
        this.approve = approve;
    }

    public Goods getGoods() {
        return goods;
    }

    public void setGoods(Goods goods) {
        this.goods = goods;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProviderId() {
        return providerId;
    }

    public void setProviderId(Integer providerId) {
        this.providerId = providerId;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getOutputTime() {
        return outputTime;
    }

    public void setOutputTime(String outputTime) {
        this.outputTime = outputTime;
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

    public String getApproveTime() {
        return approveTime;
    }

    public void setApproveTime(String approveTime) {
        this.approveTime = approveTime;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}
