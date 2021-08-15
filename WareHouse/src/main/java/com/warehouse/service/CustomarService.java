package com.warehouse.service;

import com.warehouse.pojo.Customar;

import java.util.List;

public interface CustomarService {
    public int getcustomarState();
//    全查客户
    public List<Customar> getCustomarList(int page, int limit);
//查看客户数量
    public int getCustomarcount();
//修改客户
    int updateCustomer(int id, String customerName, String zip, String tel, String address, String contactName, String contactTel, String bank, String account, String email, int state);
//    添加客户
    boolean addCustomar(
            String customerName,
            String zip,
            String tel,
            String address,
            String contactName,
            String contactTel,
            String email,
            String bank,
            String account,
            Integer state);

//    删除客户
    int deleteCustomar(int id);

//   模糊查询
    Customar selectById(String customerName);
}
