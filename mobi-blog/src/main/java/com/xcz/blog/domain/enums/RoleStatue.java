package com.xcz.blog.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleStatue {
    CUSTOMER(1),
    ADMIN(2),
    MASTER(3),
    ;
    private final int value;

    /**
     * 获取角色枚举
     * @param value 角色value
     * @return      枚举（没找到默认为游客，防止系统访问出现问题）
     */
    public static RoleStatue getRoleStatus(int value) {
        for (RoleStatue roleStatue : RoleStatue.values()) {
            if (roleStatue.getValue() == value) {
                return roleStatue;
            }
        }
        return CUSTOMER;
    }

    /**
     * 判断是否可以发布文章
     * @param role  角色
     * @return      true可以|false不可以
     */
    public static boolean isCanPublish(int role){
        return role == ADMIN.value ||  role == MASTER.value;
    }

    /**
     * 判断是否为超级管理员
     *
     * @param role 角色值
     * @return 是否为 MASTER
     */
    public static boolean isMaster(int role) {
        return role == MASTER.value;
    }

    /**
     * 判断是否为普通管理员
     *
     * @param role 角色值
     * @return 是否为 ADMIN
     */
    public static boolean isAdmin(int role) {
        return role == ADMIN.value;
    }
}
