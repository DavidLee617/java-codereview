package com.warehouse.config;

import com.warehouse.realm.StaffRealm;
import com.warehouse.realm.nopassword.MyRetryLimitCredentialsMatcher;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;
@Configuration
public class ShiroConfig {
    /**
     * 创建ShiroFilterFactoryBean
     */
    @Bean
    public ShiroFilterFactoryBean getShiroFilterFactoryBean(SecurityManager securityManager){
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        //设置安全管理器
        shiroFilterFactoryBean.setSecurityManager(securityManager);

        //添加shiro内置过滤器
        Map<String,String> filterMap = new LinkedHashMap<>();
        filterMap.put("/login","anon");
        filterMap.put("/mobileLogin","anon");
        filterMap.put("/getSession","anon");
        filterMap.put("/forget","anon");
        filterMap.put("/tel","anon");
        filterMap.put("/verifyUser","anon");
        filterMap.put("/sendCaptcha","anon");
        filterMap.put("/resetPassword","anon");
        filterMap.put("/url","anon");
        //授权过滤器
        //注意：当前授权拦截后，shiro会自动调到未授权页面
//        filterMap.put("/staff","perms[staff:staff]");
//        filterMap.put("/Role","perms[role:role]");
        filterMap.put("/*","authc");
        //登录页面
        shiroFilterFactoryBean.setLoginUrl("/toLogin");
        //设置未授权页面
//        shiroFilterFactoryBean.setUnauthorizedUrl("/unAuth");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterMap);

        return shiroFilterFactoryBean;
    }
    /**
     * 创建DefaultWebSecurityManager
     */
    @Bean
    public DefaultWebSecurityManager getDefaultWebSecurityManager(@Qualifier("codLimitCredentialsMatcher")MyRetryLimitCredentialsMatcher matcher){
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        //关联realm
        securityManager.setRealm(getRealm(matcher));
        return securityManager;
    }

    /**
     * 创建Realm
     */
    @Bean(name = "staffRealm")
    public StaffRealm getRealm(@Qualifier("codLimitCredentialsMatcher") MyRetryLimitCredentialsMatcher matcher){
        StaffRealm staffRealm=new StaffRealm();
        staffRealm.setCredentialsMatcher(matcher);
        return staffRealm;
    }

    /**
     * 密码匹配凭证管理器
     *
     * @注意 这里的加密算法 得跟添加用户时的加密算法一致
     * 这里采用md5散列一次，所以在工具类EncryptUtil中也md5散列一次
     */
    @Bean(name = "codLimitCredentialsMatcher")
    public MyRetryLimitCredentialsMatcher hashedCredentialsMatcher() {
        return new MyRetryLimitCredentialsMatcher();
    }
}
