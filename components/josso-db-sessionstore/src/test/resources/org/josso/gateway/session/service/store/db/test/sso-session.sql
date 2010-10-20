CREATE TABLE JOSSO_SESSION
(
    SESSION_ID                       VARCHAR (64)                   PRIMARY KEY
  , USERNAME                         VARCHAR (128)                  NOT NULL
  , CREATION_TIME                    BIGINT                         NOT NULL
  , LAST_ACCESS_TIME                 BIGINT                         NOT NULL
  , ACCESS_COUNT                     INT                            NOT NULL
  , MAX_INACTIVE_INTERVAL            INT                            NOT NULL
  , VALID                            INT                            NOT NULL
);
