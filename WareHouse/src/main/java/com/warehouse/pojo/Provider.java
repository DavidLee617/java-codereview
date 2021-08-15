package com.warehouse.pojo;

public class Provider {
    private Integer id;
    private String providername;
    private String zip;
    private String address;
    private String tel;
    private String contactname;
    private String contacttel;
    private String bank;
    private String account;
    private String email;
    private Integer state;

    public Provider() {
    }

    public Provider(Integer id, String providername, String zip, String address, String tel, String contactname, String contacttel, String bank, String account, String email, Integer state) {
        this.id = id;
        this.providername = providername;
        this.zip = zip;
        this.address = address;
        this.tel = tel;
        this.contactname = contactname;
        this.contacttel = contacttel;
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

    public String getProvidername() {
        return providername;
    }

    public void setProvidername(String providername) {
        this.providername = providername;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getContactname() {
        return contactname;
    }

    public void setContactname(String contactname) {
        this.contactname = contactname;
    }

    public String getContacttel() {
        return contacttel;
    }

    public void setContacttel(String contacttel) {
        this.contacttel = contacttel;
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
