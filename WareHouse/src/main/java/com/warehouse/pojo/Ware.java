package com.warehouse.pojo;

public class Ware {
    private Integer id;
    private String wareName;
    private Integer locationId;
    private String locationName;
    private Location location;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    private String goodsName;

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Ware(Integer id, String wareName, Integer locationId, String goodsName, String locationName) {
        this.id = id;
        this.wareName = wareName;
        this.locationId = locationId;
        this.goodsName = goodsName;
        this.locationName = locationName;
    }

    public Ware() {
    }
    public Ware(JSONObject json){
        this.id=json.getInt("id");
        this.wareName=json.getString("goodsName");
        this.locationId=json.getInt("locationId");
        this.goodsName=json.getString("goodsName");
        this.locationName=json.getString("locationName");
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getWareName() {
        return wareName;
    }

    public void setWareName(String wareName) {
        this.wareName = wareName;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }
}
