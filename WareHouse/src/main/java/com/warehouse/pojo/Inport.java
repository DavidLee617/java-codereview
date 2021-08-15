package com.warehouse.pojo;

public class Inport {
    private Integer id;
    private Integer providerId;
    private String payType;
    private String approveTime;
    private String inportTime;
    private String operateName;
    private String warehouseName;
    private Integer number;
    private String remark;
    private double inportPrice;
    private Integer goodsId;
    private Integer state;
    private String reason;
    private String goodsSize;
    private Goods goods;
    private Provider provider;
    private Approve approve;

    public Inport() {
    }

    public Inport(Integer id, Integer providerId, String payType, String approveTime,
                  String inportTime, String operateName, String warehouseName,
                  Integer number, String remark, double inportPrice, Integer goodsId,
                  Integer state, String reason, String goodsSize, Goods goods,
                  Provider provider) {
        this.id = id;
        this.providerId = providerId;
        this.payType = payType;
        this.approveTime = approveTime;
        this.inportTime = inportTime;
        this.operateName = operateName;
        this.warehouseName = warehouseName;
        this.number = number;
        this.remark = remark;
        this.inportPrice = inportPrice;
        this.goodsId = goodsId;
        this.state = state;
        this.reason = reason;
        this.goodsSize = goodsSize;
        this.goods = goods;
        this.provider = provider;
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

    public String getGoodsSize() {
        return goodsSize;
    }

    public void setGoodsSize(String goodsSize) {
        this.goodsSize = goodsSize;
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

    public String getApproveTime() {
        return approveTime;
    }

    public void setApproveTime(String approveTime) {
        this.approveTime = approveTime;
    }

    public String getInportTime() {
        return inportTime;
    }

    public void setInportTime(String inportTime) {
        this.inportTime = inportTime;
    }

    public String getOperateName() {
        return operateName;
    }

    public void setOperateName(String operateName) {
        this.operateName = operateName;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public double getInportPrice() {
        return inportPrice;
    }

    public void setInportPrice(double inportPrice) {
        this.inportPrice = inportPrice;
    }

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
