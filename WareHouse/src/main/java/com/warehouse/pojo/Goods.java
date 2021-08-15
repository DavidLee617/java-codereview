package com.warehouse.pojo;

public class Goods {
    private Integer id;
    private String goodsName;
    private String producePlace;
    private String goodsType;
    private String size;
    private Integer packageId;
    private String type;
    private String productCode;
    private String promitCode;
    private String description;
    private double inportprice;
    private double salesprice;
    private Integer providerId;
    private String providername;
    private Integer state;
    private Integer number;
    private String goodsImg;
    private Integer wareId;
    private String wareName;
    private Integer locationId;
    private String locationName;
    private Salesback salesback;

    public Goods() {
    }

    public Goods(Integer id, String goodsName, String producePlace, String goodsType, String size, Integer packageId, String type, String productCode, String promitCode, String description, double inportprice, double salesprice, Integer providerId, String providername, Integer state, Integer number, String goodsImg, Integer wareId, String wareName, Integer locationId, String locationName) {
        this.id = id;
        this.goodsName = goodsName;
        this.producePlace = producePlace;
        this.goodsType = goodsType;
        this.size = size;
        this.packageId = packageId;
        this.type = type;
        this.productCode = productCode;
        this.promitCode = promitCode;
        this.description = description;
        this.inportprice = inportprice;
        this.salesprice = salesprice;
        this.providerId = providerId;
        this.providername = providername;
        this.state = state;
        this.number = number;
        this.goodsImg = goodsImg;
        this.wareId = wareId;
        this.wareName = wareName;
        this.locationId = locationId;
        this.locationName = locationName;
    }

    public Salesback getSalesback() {
        return salesback;
    }

    public void setSalesback(Salesback salesback) {
        this.salesback = salesback;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProvidername() {
        return providername;
    }

    public void setProvidername(String providername) {
        this.providername = providername;
    }

    public void setGoodsImg(String goodsImg) {
        this.goodsImg = goodsImg;
    }

    public String getWareName() {
        return wareName;
    }

    public void setWareName(String wareName) {
        this.wareName = wareName;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getProducePlace() {
        return producePlace;
    }

    public void setProducePlace(String producePlace) {
        this.producePlace = producePlace;
    }

    public String getGoodsType() {
        return goodsType;
    }

    public void setGoodsType(String goodsType) {
        this.goodsType = goodsType;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Integer getPackageId() {
        return packageId;
    }

    public void setPackageId(Integer packageId) {
        this.packageId = packageId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getPromitCode() {
        return promitCode;
    }

    public void setPromitCode(String promitCode) {
        this.promitCode = promitCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getInportprice() {
        return inportprice;
    }

    public void setInportprice(double inportprice) {
        this.inportprice = inportprice;
    }

    public double getSalesprice() {
        return salesprice;
    }

    public void setSalesprice(double salesprice) {
        this.salesprice = salesprice;
    }

    public Integer getProviderId() {
        return providerId;
    }

    public void setProviderId(Integer providerId) {
        this.providerId = providerId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getGoodsImg() {
        return goodsImg;
    }

    public void setGoodslmg(String goodsImg) {
        this.goodsImg = goodsImg;
    }

    public Integer getWareId() {
        return wareId;
    }

    public void setWareId(Integer wareId) {
        this.wareId = wareId;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }
}
