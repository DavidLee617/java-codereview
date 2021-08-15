package com.warehouse.pojo;

public class Log {
    private Integer id;
    private String name;
    private String ip;
    private String time;

    public Log() {
    }

    public Log(Integer id, String name, String ip, String time) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.time = time;
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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
