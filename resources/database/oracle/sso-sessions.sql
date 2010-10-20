--
-- Sample DB schema to store JOSSO sessions
--
-- Version: $Id: sso-sessions.sql 602 2008-08-20 23:58:11Z gbrigand $
--

-- SSO Sessions

CREATE TABLE JOSSO_SESSION
(
    SESSION_ID                       VARCHAR2 (64)                   NOT NULL
  , USERNAME                         VARCHAR2 (128)                  NOT NULL
  , CREATION_TIME                    INTEGER                         NOT NULL
  , LAST_ACCESS_TIME                 INTEGER                         NOT NULL
  , ACCESS_COUNT                     INTEGER                         NOT NULL
  , MAX_INACTIVE_INTERVAL            INTEGER                         NOT NULL
  , VALID                            INTEGER                         NOT NULL
);

ALTER TABLE JOSSO_SESSION
       ADD  ( PRIMARY KEY (SESSION_ID) ) ;

