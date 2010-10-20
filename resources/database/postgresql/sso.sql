--
-- Sample DB schema to store users and their roles.
-- 
-- Version: $Id: sso.sql 543 2008-03-18 21:34:58Z sgonzalez $
--

-- Roles

CREATE TABLE JOSSO_ROLE (
       NAME                 VARCHAR(16) NOT NULL,
       DESCRIPTION          VARCHAR(64) NULL
);

ALTER TABLE JOSSO_ROLE
       ADD  PRIMARY KEY (NAME) ;


-- Users

CREATE TABLE JOSSO_USER (
       LOGIN                VARCHAR(16) NOT NULL,
       PASSWORD             VARCHAR(20) NOT NULL,
       NAME                 VARCHAR(64) NULL,
       DESCRIPTION          VARCHAR(64) NULL
);

ALTER TABLE JOSSO_USER
       ADD  PRIMARY KEY (LOGIN);


-- Users Properties

CREATE TABLE JOSSO_USER_PROPERTY (
       LOGIN                VARCHAR(16) NOT NULL,
       NAME                 VARCHAR(255) NOT NULL,
       VALUE                VARCHAR(255) NOT NULL
);

ALTER TABLE JOSSO_USER_PROPERTY
       ADD  PRIMARY KEY (LOGIN, NAME) ;

ALTER TABLE JOSSO_USER_PROPERTY
       ADD  FOREIGN KEY (LOGIN)
                             REFERENCES JOSSO_USER ;

-- Roles by user

CREATE TABLE JOSSO_USER_ROLE (
       LOGIN                VARCHAR(16) NOT NULL,
       NAME                 VARCHAR(255) NOT NULL
);


ALTER TABLE JOSSO_USER_ROLE
       ADD  PRIMARY KEY (LOGIN, NAME);


ALTER TABLE JOSSO_USER_ROLE
       ADD  FOREIGN KEY (NAME)
                             REFERENCES JOSSO_ROLE ;

ALTER TABLE JOSSO_USER_ROLE
       ADD  FOREIGN KEY (LOGIN)
                             REFERENCES JOSSO_USER ;
