package com.warehouse.dao;

import com.warehouse.pojo.Provider;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProviderDao {
    public int getProviderState();

//    全查供应商
    public List<Provider> getProviderList(int page, int limit);
//查看供应商数量
    public int getProvidercount();
//修改供应商
    int updateProvider(int id, String providername, String zip, String address, String tel, String contactname, String contacttel, String bank, String account, String email, int state);
//    添加供应商
    Integer addProvider(
            @Param("providername") String providername,
            @Param("zip") String zip,
            @Param("address") String address,
            @Param("tel") String tel,
            @Param("contactname") String contactname,
            @Param("contacttel") String contacttel,
            @Param("bank") String bank,
            @Param("account") String account,
            @Param("email") String email,
            @Param("state") Integer state
    );

//模糊查询
    Provider selectById(String providername);
}
