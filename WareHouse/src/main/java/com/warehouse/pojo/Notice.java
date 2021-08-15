package com.warehouse.pojo;

public class Notice {
    private Integer id;
    private String title;
    private String context;
    private String time;
    private String operateName;

    public Notice() {
    }

    public Notice(Integer id, String title, String context, String time, String operateName) {
        this.id = id;
        this.title = title;
        this.context = context;
        this.time = time;
        this.operateName = operateName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getOperateName() {
        return operateName;
    }

    public void setOperateName(String operateName) {
        this.operateName = operateName;
    }
}
