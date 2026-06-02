package com.familyaccount.common;

/**
 * 用户上下文 — 基于 ThreadLocal，在整个请求生命周期中持有当前用户信息
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<Long> FAMILY_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> ROLE = new ThreadLocal<>();
    private static final ThreadLocal<Long> MEMBER_ID = new ThreadLocal<>();

    public static void setUserId(Long userId) { USER_ID.set(userId); }
    public static Long getUserId() { return USER_ID.get(); }

    public static void setUsername(String username) { USERNAME.set(username); }
    public static String getUsername() { return USERNAME.get(); }

    public static void setFamilyId(Long familyId) { FAMILY_ID.set(familyId); }
    public static Long getFamilyId() { return FAMILY_ID.get(); }

    public static void setRole(String role) { ROLE.set(role); }
    public static String getRole() { return ROLE.get(); }

    public static void setMemberId(Long memberId) { MEMBER_ID.set(memberId); }
    public static Long getMemberId() { return MEMBER_ID.get(); }

    /**
     * 请求结束后必须调用，防止内存泄漏
     */
    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
        FAMILY_ID.remove();
        ROLE.remove();
        MEMBER_ID.remove();
    }
}
