INSERT INTO JOSSO_ROLE VALUES ('role1', NULL);
INSERT INTO JOSSO_ROLE VALUES ('role2', NULL);

INSERT INTO JOSSO_USER VALUES ('user1','7ea2bd72bfc7dabdfecc0b5760ebcf52', 'user1@josso.org', 'User 1 full name', 'josso user');
INSERT INTO JOSSO_USER VALUES ('user2','3d281d21c49d79f586af2cdc4419b18b', 'user2@josso.org', 'User 2 full name', 'josso user');

INSERT INTO JOSSO_USER_PROPERTY VALUES ('user1', 'name', 'User 1 First Name');
INSERT INTO JOSSO_USER_PROPERTY VALUES ('user1', 'lastName', 'User 1 Last Name');
INSERT INTO JOSSO_USER_PROPERTY VALUES ('user1', 'registrationDate', '2004/09/11');
INSERT INTO JOSSO_USER_PROPERTY VALUES ('user2', 'name', 'User 2 First Name');
INSERT INTO JOSSO_USER_PROPERTY VALUES ('user2', 'lastName', 'User 2 Last Name');
INSERT INTO JOSSO_USER_PROPERTY VALUES ('user2', 'registrationDate', '2004/09/10');

INSERT INTO JOSSO_USER_ROLE VALUES ('user1', 'role1');
INSERT INTO JOSSO_USER_ROLE VALUES ('user1', 'role2');
INSERT INTO JOSSO_USER_ROLE VALUES ('user2', 'role2');

COMMIT;