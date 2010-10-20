--
-- Sample DB schema to store JOSSO sessions
--
-- Version: $Id: sso-sessions.sql 543 2008-03-18 21:34:58Z sgonzalez $
--

-- SSO Sessions

CREATE TABLE JOSSO_SESSION
(
    SESSION_ID                       VARCHAR (64)                   NOT NULL
  , USERNAME                         VARCHAR (128)                  NOT NULL
  , CREATION_TIME                    INT8                         NOT NULL
  , LAST_ACCESS_TIME                 INT8                         NOT NULL
  , ACCESS_COUNT                     INTEGER                         NOT NULL
  , MAX_INACTIVE_INTERVAL            INTEGER                         NOT NULL
  , VALID                            BOOLEAN                         NOT NULL
);

ALTER TABLE JOSSO_SESSION
       ADD  PRIMARY KEY (SESSION_ID);

