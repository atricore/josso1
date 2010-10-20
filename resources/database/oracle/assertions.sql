--
-- Sample DB schema to store JOSSO authentication assertions
--
-- Version: $Id$
--

-- Authentication Assertions

CREATE TABLE JOSSO_ASSERTION
(
    ASSERTION_ID                     VARCHAR2 (64)              NOT NULL
  , SECURITY_DOMAIN_NAME             VARCHAR2 (64)              NOT NULL
  , SSO_SESSION_ID                   VARCHAR2 (64)              NOT NULL
  , CREATION_TIME                    INTEGER                    NOT NULL
  , VALID                            INTEGER                    NOT NULL
);

ALTER TABLE JOSSO_ASSERTION
       ADD  ( PRIMARY KEY (ASSERTION_ID) ) ;

