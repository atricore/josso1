--
-- Sample DB schema to store authentication assertions
--
-- Version: $Id$
--

-- Assertions

CREATE TABLE JOSSO_ASSERTION
(
    ASSERTION_ID                     VARCHAR (64)                   NOT NULL
  , SECURITY_DOMAIN_NAME             VARCHAR (64)                   NOT NULL
  , SSO_SESSION_ID                   VARCHAR (64)                   NOT NULL
  , CREATION_TIME                    INT8                           NOT NULL
  , VALID                            BOOLEAN                        NOT NULL
);

ALTER TABLE JOSSO_ASSERTION
       ADD  PRIMARY KEY (ASSERTION_ID);

