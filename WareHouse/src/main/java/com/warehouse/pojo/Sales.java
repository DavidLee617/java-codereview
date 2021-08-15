package com.warehouse.pojo;

public class Sales {
    private Integer id;
    private Integer customerId;
    private String payType;
    private String salesTime;
    private String operateName;
    private Integer number;
    private double salePrice;
    private Integer goodsId;
    private String remark;
    private String warehouseName;
    private String approveTime;
    private Integer state;
    private Goods goods;
    private Customar customar;
    private Approve approve;

    public Sales(Integer id, Integer customerId, String payType, String salesTime, String operateName, Integer number, double salePrice, Integer goodsId, String remark, String warehouseName, String approveTime, Integer state, Goods goods, Customar customar, Approve approve) {
        this.id = id;
        this.customerId = customerId;
        this.payType = payType;
        this.salesTime = salesTime;
        this.operateName = operateName;
        this.number = number;
        this.salePrice = salePrice;
        this.goodsId = goodsId;
        this.remark = remark;
        this.warehouseName = warehouseName;
        this.approveTime = approveTime;
        this.state = state;
        this.goods = goods;
        this.customar = customar;
        this.approve = approve;
    }

    public Sales() {
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

    public String getSalesTime() {
        return salesTime;
    }

    public void setSalesTime(String salesTime) {
        this.salesTime = salesTime;
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

    public double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(double salePrice) {
        this.salePrice = salePrice;
    }

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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
