CREATE TABLE JOSSO_ASSERTION
(
    ASSERTION_ID                     VARCHAR (64)              PRIMARY KEY
  , SECURITY_DOMAIN_NAME             VARCHAR (64)              NOT NULL
  , SSO_SESSION_ID                   VARCHAR (64)              NOT NULL
  , CREATION_TIME                    BIGINT                    NOT NULL
  , VALID                            INT                       NOT NULL
);
