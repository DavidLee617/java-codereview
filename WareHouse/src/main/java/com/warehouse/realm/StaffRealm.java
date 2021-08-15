package com.warehouse.realm;

import com.warehouse.pojo.Permission;
import com.warehouse.pojo.Role;
import com.warehouse.pojo.Staff;
import com.warehouse.realm.nopassword.CustomToken;
import com.warehouse.service.StaffService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class StaffRealm extends AuthorizingRealm {
    @Autowired
    private StaffService staffService;
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        Subject subject = SecurityUtils.getSubject();
        Staff staff1 = (Staff) subject.getPrincipal();
        List<Staff> staffList = staffService.getStaffListByName(staff1.getJobNumber());
        String roleName = null;
        for (Staff staff : staffList){
                roleName =staff.getRole().getName();
                info.addRole(roleName);
        }
        List<Role> roleList = staffService.getRoleByName(roleName);
        for (Role role1 : roleList){
            for (Permission p :role1.getPermission()){
                info.addStringPermission(p.getP_name());
            }
        }
        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        CustomToken token = (CustomToken)authenticationToken;
        String username = null;
        try {
            username = (String) token.getPrincipal();
        } catch (Exception e) {
            throw new AuthenticationException();
        }
        Staff staff = staffService.getStaffByTel(username);
        if(staff == null){
            staff = staffService.getStaff(username);
            if(staff == null){
                return null;
            }
        }
        return new SimpleAuthenticationInfo(staff,staff.getPassword(),"");
    }
}
