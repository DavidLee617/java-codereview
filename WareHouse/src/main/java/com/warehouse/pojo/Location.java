package com.warehouse.pojo;

public class Location {
    private Integer id;
    private String locationName;
    private Integer wareId;

    public Location(Integer id, String locationName, Integer wareId) {
        this.id = id;
        this.locationName = locationName;
        this.wareId = wareId;
    }

    public Integer getWareId() {
        return wareId;
    }

    public void setWareId(Integer wareId) {
        this.wareId = wareId;
    }

    public Location() {
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
}
