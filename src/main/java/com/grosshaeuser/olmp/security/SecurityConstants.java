package com.grosshaeuser.olmp.security;

public final class SecurityConstants {
    private SecurityConstants() { }

    public static final class Roles {
        private Roles() { }

        public static final String ROOT_ADMIN = "ROOT_ADMIN";
        public static final String ADMIN = "ADMIN";
        public static final String STAFF = "STAFF";
        public static final String MEMBER = "MEMBER";
    }

    public static final class Privileges {

        private Privileges() { }

        public static final String VERIFY_MEMBER = "MEMBER:VERIFY";
        public static final String CREATE_MEMBERSHIP = "MEMBER:CREATE_MEMBERSHIP";
        public static final String READ_MEMBERSHIP = "MEMBER:READ_MEMBERSHIP";
        public static final String UPDATE_MEMBERSHIP = "MEMBER:UPDATE_MEMBERSHIP";
        public static final String CANCEL_MEMBERSHIP = "MEMBER:CANCEL_MEMBERSHIP";

        public static final String ENABLE_USER = "USER:ENABLE";
        public static final String DISABLE_USER = "USER:DISABLE";
        public static final String LOCK_USER = "USER:LOCK";
        public static final String UNLOCK_USER = "USER:UNLOCK";

        public static final String RESET_PASSWORD = "USER:RESET_PASSWORD";

        public static final String CREATE_ADMIN_ACCOUNT = "USER:CREATE_ADMIN_ACCOUNT";
        public static final String READ_ADMIN_ACCOUNT = "USER:READ_ADMIN_ACCOUNT";
        public static final String UPDATE_ADMIN_ACCOUNT = "USER:UPDATE_ADMIN_ACCOUNT";
        public static final String DELETE_ADMIN_ACCOUNT = "USER:DELETE_ADMIN_ACCOUNT";

        public static final String CREATE_STAFF_ACCOUNT = "USER:CREATE_STAFF_ACCOUNT";
        public static final String READ_STAFF_ACCOUNT = "USER:READ_STAFF_ACCOUNT";
        public static final String UPDATE_STAFF_ACCOUNT = "USER:UPDATE_STAFF_ACCOUNT";
        public static final String DELETE_STAFF_ACCOUNT = "USER:DELETE_STAFF_ACCOUNT";

        public static final String CREATE_MEMBER_ACCOUNT = "USER:CREATE_MEMBER_ACCOUNT";
        public static final String READ_MEMBER_ACCOUNT = "USER:READ_MEMBER_ACCOUNT";
        public static final String UPDATE_MEMBER_ACCOUNT = "USER:UPDATE_MEMBER_ACCOUNT";
        public static final String DELETE_MEMBER_ACCOUNT = "USER:DELETE_MEMBER_ACCOUNT";

        public static final String ASSIGN_MEMBER_ROLE = "USER:ASSIGN_MEMBER_ROLE";
        public static final String REVOKE_MEMBER_ROLE = "USER:REVOKE_MEMBER_ROLE";
        public static final String ASSIGN_STAFF_ROLE = "USER:ASSIGN_STAFF_ROLE";
        public static final String REVOKE_STAFF_ROLE = "USER:REVOKE_STAFF_ROLE";
        public static final String ASSIGN_ADMIN_ROLE = "USER:ASSIGN_ADMIN_ROLE";
        public static final String REVOKE_ADMIN_ROLE = "USER:REVOKE_ADMIN_ROLE";
    }
}
