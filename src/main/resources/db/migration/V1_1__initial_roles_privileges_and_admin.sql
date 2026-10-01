INSERT INTO roles (name) VALUES ('USER');
INSERT INTO roles (name) VALUES ('PATRON');
INSERT INTO roles (name) VALUES ('STAFF');
INSERT INTO roles (name) VALUES ('ADMIN');
INSERT INTO roles (name) VALUES ('SUPER_ADMIN');
INSERT INTO roles (name) VALUES ('INSTITUTION');



INSERT INTO privileges (name) VALUES ('CATALOG:READ');
INSERT INTO privileges (name) VALUES ('CATALOG:EDIT');
INSERT INTO privileges (name) VALUES ('CATALOG:MANAGE_CATEGORIES');

INSERT INTO privileges (name) VALUES ('ITEM:READ');
INSERT INTO privileges (name) VALUES ('ITEM:CREATE');
INSERT INTO privileges (name) VALUES ('ITEM:EDIT');
INSERT INTO privileges (name) VALUES ('ITEM:DELETE');
INSERT INTO privileges (name) VALUES ('ITEM:MARK_LOST');
INSERT INTO privileges (name) VALUES ('ITEM:MARK_DAMAGED');
INSERT INTO privileges (name) VALUES ('ITEM:MANAGE_AVAILABILITY');

INSERT INTO privileges (name) VALUES ('LOAN:BORROW_SELF');
INSERT INTO privileges (name) VALUES ('LOAN:RETURN_SELF');
INSERT INTO privileges (name) VALUES ('LOAN:RESERVE_SELF');
INSERT INTO privileges (name) VALUES ('LOAN:RENEW_SELF');
INSERT INTO privileges (name) VALUES ('LOAN:BORROW_ON_BEHALF');
INSERT INTO privileges (name) VALUES ('LOAN:RETURN_ANY');
INSERT INTO privileges (name) VALUES ('LOAN:RESERVE_ON_BEHALF');
INSERT INTO privileges (name) VALUES ('LOAN:RENEW_ANY');
INSERT INTO privileges (name) VALUES ('LOAN:VIEW_HISTORY_SELF');
INSERT INTO privileges (name) VALUES ('LOAN:VIEW_HISTORY_ALL');

INSERT INTO privileges (name) VALUES ('USER:READ_SELF');
INSERT INTO privileges (name) VALUES ('USER:EDIT_SELF');
INSERT INTO privileges (name) VALUES ('USER:DELETE_SELF');
INSERT INTO privileges (name) VALUES ('USER:READ_SUBSCRIPTION_SELF');
INSERT INTO privileges (name) VALUES ('USER:SUBSCRIBE_SELF');
INSERT INTO privileges (name) VALUES ('USER:CANCEL_SUBSCRIPTION_SELF');
INSERT INTO privileges (name) VALUES ('USER:READ_ALL');
INSERT INTO privileges (name) VALUES ('USER:EDIT_ALL');
INSERT INTO privileges (name) VALUES ('USER:DISABLE');
INSERT INTO privileges (name) VALUES ('USER:VERIFY');
INSERT INTO privileges (name) VALUES ('USER:RESET_PASSWORD');
INSERT INTO privileges (name) VALUES ('USER:CREATE_SUBSCRIPTION_ON_BEHALF');
INSERT INTO privileges (name) VALUES ('USER:CANCEL_SUBSCRIPTION_ON_BEHALF');
INSERT INTO privileges (name) VALUES ('USER:EDIT_SUBSCRIPTION');
INSERT INTO privileges (name) VALUES ('USER:DELETE_SUBSCRIPTION');
INSERT INTO privileges (name) VALUES ('USER:MANAGE_ROLES');

INSERT INTO privileges (name) VALUES ('FINES:VIEW_SELF');
INSERT INTO privileges (name) VALUES ('FINES:VIEW_ALL');
INSERT INTO privileges (name) VALUES ('FINES:WAIVE');
INSERT INTO privileges (name) VALUES ('FINES:ADJUST');
INSERT INTO privileges (name) VALUES ('FINES:REFUND');
INSERT INTO privileges (name) VALUES ('FINES:PROCESS_PAYMENT');

INSERT INTO privileges (name) VALUES ('API:READ_CATALOG');
INSERT INTO privileges (name) VALUES ('API:MANAGE_KEYS_SELF');
INSERT INTO privileges (name) VALUES ('API:MANAGE_KEYS_ALL');

INSERT INTO privileges (name) VALUES ('SYSTEM:CREATE_ADMIN_ACCOUNT');
INSERT INTO privileges (name) VALUES ('SYSTEM:READ_ADMIN_ACCOUNT');
INSERT INTO privileges (name) VALUES ('SYSTEM:UPDATE_ADMIN_ACCOUNT');
INSERT INTO privileges (name) VALUES ('SYSTEM:DELETE_ADMIN_ACCOUNT');
INSERT INTO privileges (name) VALUES ('SYSTEM:CREATE_STAFF_ACCOUNT');
INSERT INTO privileges (name) VALUES ('SYSTEM:READ_STAFF_ACCOUNT');
INSERT INTO privileges (name) VALUES ('SYSTEM:UPDATE_STAFF_ACCOUNT');
INSERT INTO privileges (name) VALUES ('SYSTEM:DELETE_STAFF_ACCOUNT');
INSERT INTO privileges (name) VALUES ('SYSTEM:ASSIGN_STAFF_ROLE');
INSERT INTO privileges (name) VALUES ('SYSTEM:REVOKE_STAFF_ROLE');
INSERT INTO privileges (name) VALUES ('SYSTEM:ASSIGN_ADMIN_ROLE');
INSERT INTO privileges (name) VALUES ('SYSTEM:REVOKE_ADMIN_ROLE');

INSERT INTO privileges (name) VALUES ('AUDIT:READ');
INSERT INTO privileges (name) VALUES ('REPORT:GENERATE');
INSERT INTO privileges (name) VALUES ('REPORT:READ');
INSERT INTO privileges (name) VALUES ('REPORT:EXPORT');



INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE name = 'USER'), id FROM privileges
WHERE name IN (
               'CATALOG:READ',
               'ITEM:READ',
               'LOAN:VIEW_HISTORY_SELF',
               'USER:READ_SELF',
               'USER:EDIT_SELF',
               'USER:DELETE_SELF',
               'USER:READ_SUBSCRIPTION_SELF',
               'USER:SUBSCRIBE_SELF',
               'USER:CANCEL_SUBSCRIPTION_SELF'
                   'FINES:VIEW_SELF'
    );

-- In addition to USER privileges
INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE name = 'PATRON'), id FROM privileges
WHERE name IN (
               'LOAN:BORROW_SELF',
               'LOAN:RETURN_SELF',
               'LOAN:RESERVE_SELF',
               'LOAN:RENEW_SELF'
    );

INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE name = 'STAFF'), id FROM privileges
WHERE name IN (
               'CATALOG:READ',
               'CATALOG:EDIT',
               'CATALOG:MANAGE_CATEGORIES',
               'ITEM:READ',
               'ITEM:CREATE',
               'ITEM:EDIT',
               'ITEM:DELETE',
               'ITEM:MARK_LOST',
               'ITEM:MARK_DAMAGED',
               'ITEM:MANAGE_AVAILABILITY',
               'LOAN:BORROW_ON_BEHALF',
               'LOAN:RETURN_ANY',
               'LOAN:RESERVE_ON_BEHALF',
               'LOAN:RENEW_ANY',
               'LOAN:VIEW_HISTORY_ALL',
               'USER:READ_ALL',
               'USER:EDIT_ALL',
               'USER:DISABLE',
               'USER:VERIFY',
               'USER:RESET_PASSWORD',
               'USER:CREATE_SUBSCRIPTION_ON_BEHALF',
               'USER:CANCEL_SUBSCRIPTION_ON_BEHALF',
               'USER:EDIT_SUBSCRIPTION',
               'USER:DELETE_SUBSCRIPTION',
               'USER:MANAGE_ROLES',
               'FINES:VIEW_ALL',
               'FINES:PROCESS_PAYMENT'
    );

-- In addition to STAFF privileges
INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE name = 'ADMIN'), id FROM privileges
WHERE name IN (
               'FINES:WAIVE',
               'FINES:ADJUST',
               'FINES:REFUND',
               'SYSTEM:CREATE_STAFF_ACCOUNT',
               'SYSTEM:READ_STAFF_ACCOUNT',
               'SYSTEM:UPDATE_STAFF_ACCOUNT',
               'SYSTEM:DELETE_STAFF_ACCOUNT',
               'SYSTEM:ASSIGN_STAFF_ROLE',
               'SYSTEM:REVOKE_STAFF_ROLE',
               'AUDIT:READ',
               'REPORT:GENERATE',
               'REPORT:READ',
               'REPORT:EXPORT',
               'API:MANAGE_KEYS_ALL'
    );

INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE name = 'SUPER_ADMIN'), id FROM privileges
WHERE name IN (
               'SYSTEM:CREATE_ADMIN_ACCOUNT',
               'SYSTEM:READ_ADMIN_ACCOUNT',
               'SYSTEM:UPDATE_ADMIN_ACCOUNT',
               'SYSTEM:DELETE_ADMIN_ACCOUNT',
               'SYSTEM:CREATE_STAFF_ACCOUNT',
               'SYSTEM:READ_STAFF_ACCOUNT',
               'SYSTEM:UPDATE_STAFF_ACCOUNT',
               'SYSTEM:DELETE_STAFF_ACCOUNT',
               'SYSTEM:ASSIGN_STAFF_ROLE',
               'SYSTEM:REVOKE_STAFF_ROLE',
               'SYSTEM:ASSIGN_ADMIN_ROLE',
               'SYSTEM:REVOKE_ADMIN_ROLE',
               'USER:RESET_PASSWORD'
    );

INSERT INTO role_privileges (role_id, privilege_id)
SELECT (SELECT id FROM roles WHERE name = 'INSTITUTION'), id FROM privileges
WHERE name IN (
               'API:READ_CATALOG',
               'API:MANAGE_KEYS_SELF'
    );



INSERT INTO users(username, email, password, verified) VALUES ('super', 'super@example.com', '{noop}super', true);
INSERT INTO user_roles(user_id, role_id) SELECT (SELECT id FROM users WHERE username = 'super'), id FROM roles WHERE name = 'SUPER_ADMIN';
