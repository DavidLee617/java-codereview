package com.warehouse.pojo;

public class Customar {
    private Integer id;
    private String customerName;
    private String zip;
    private String tel;
    private String address;
    private String contactName;
    private String contactTel;
    private String bank;
    private String account;
    private String email;
    private Integer state;

    public Customar() {
    }

    public Customar(Integer id, String customerName, String zip, String tel, String address, String contactName, String contactTel, String bank, String account, String email, Integer state) {
        this.id = id;
        this.customerName = customerName;
        this.zip = zip;
        this.tel = tel;
        this.address = address;
        this.contactName = contactName;
        this.contactTel = contactTel;
        this.bank = bank;
        this.account = account;
        this.email = email;
        this.state = state;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactTel() {
        return contactTel;
    }

    public void setContactTel(String contactTel) {
        this.contactTel = contactTel;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}
