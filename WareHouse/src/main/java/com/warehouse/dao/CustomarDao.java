package com.warehouse.dao;

import com.warehouse.pojo.Customar;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomarDao {
//    全查客户
    public List<Customar> getCustomarList(int page, int limit);
//查看客户数量
    public int getCustomarcount();
//修改客户
    int updateCustomar(int id, String customerName, String zip, String tel, String address, String contactName, String contactTel, String bank, String account, String email, int state);
//    添加客户
    Integer addCustomar(
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
    public int deleteCustomar(int id);

//    模糊查询
    public Customar selectById(String customerName);

    public List<Customar> getCustomarList();

    public Integer updateCustomar(@Param("customerName") String customerName,
                                  @Param("zip") String zip,
                                  @Param("tel") String tel,
                                  @Param("address") String address,
                                  @Param("contactName") String contactName,
                                  @Param("contactTel") String contactTel,
                                  @Param("email") String email,
                                  @Param("bank") String bank,
                                  @Param("account") String account,
                                  @Param("state") Integer state);

    public int getcustomarState();

}
