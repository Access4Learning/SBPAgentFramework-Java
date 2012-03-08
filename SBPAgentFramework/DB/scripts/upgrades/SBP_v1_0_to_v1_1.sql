-- -----------------------------------------------------
-- Table SCF_VALID_INDICATOR
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS SCF_VALID_INDICATOR (
  DEPENDENCY_KEY_INFO_ID INTEGER  NOT NULL ,
  SIF_OBJECT_NAME VARCHAR(45) NOT NULL ,
  PRIMARY KEY (DEPENDENCY_KEY_INFO_ID, SIF_OBJECT_NAME) ,
 CONSTRAINT fk_SCF_VALID_INDICATOR_SCF_OBJECT1
    FOREIGN KEY (SIF_OBJECT_NAME )
    REFERENCES SCF_OBJECT (SIF_OBJECT_NAME )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_SCF_VALID_INDICATOR_SCF_DEPENDENCY_KEY_INFO1
    FOREIGN KEY (DEPENDENCY_KEY_INFO_ID )
    REFERENCES SCF_DEPENDENCY_KEY_INFO (DEPENDENCY_KEY_INFO_ID )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

CREATE INDEX fk_SCF_VALID_INDICATOR_SCF_OBJECT1 ON SCF_VALID_INDICATOR (SIF_OBJECT_NAME ASC) ;
CREATE INDEX fk_SCF_VALID_INDICATOR_SCF_DEPENDENCY_KEY_INFO1 ON SCF_VALID_INDICATOR (DEPENDENCY_KEY_INFO_ID ASC) ;

-- -----------------------------------------------------
-- Changes for table SCF_DEPENDENCY_KEY_INFO
-- -----------------------------------------------------
alter table SCF_DEPENDENCY_KEY_INFO add IS_INDICATOR TINYINT(1) NOT NULL DEFAULT 0;

-- -----------------------------------------------------
-- Data for table SCF_OBJECT
-- -----------------------------------------------------
INSERT INTO SCF_OBJECT (SIF_OBJECT_NAME, KEY_SEPARATOR, DEFAULT_EXPIRY_IN_MINUTES, DEFAULT_EXPIRY_STRATEGY) VALUES ('Identity', NULL, 120, 'EXPIRE');

-- -----------------------------------------------------
-- Data for table SCF_OBJECT_KEY
-- -----------------------------------------------------
INSERT INTO SCF_OBJECT_KEY (OBJECT_KEY_ID, SIF_OBJECT_NAME, XPATH_TO_KEY, SORT_ORDER) VALUES (10, 'Identity', '@RefId', 1);

-- -----------------------------------------------------
-- Data for table SCF_DEPENDENCY_INFO
-- -----------------------------------------------------
INSERT INTO SCF_DEPENDENCY_INFO (DEPENDENCY_INFO_ID, SIF_OBJECT_NAME, PARENT_SIF_OBJECT_NAME, MULTIPLE, XPATH_TO_LIST) VALUES (10, 'Identity', NULL, 0, NULL);

-- -----------------------------------------------------
-- Data for table SCF_DEPENDENCY_KEY_INFO
-- -----------------------------------------------------
INSERT INTO SCF_DEPENDENCY_KEY_INFO (DEPENDENCY_KEY_INFO_ID, DEPENDENCY_INFO_ID, XPATH_TO_KEY, SORT_ORDER, IS_INDICATOR) VALUES (10, 10, 'SIF_RefId', 1, 0);
INSERT INTO SCF_DEPENDENCY_KEY_INFO (DEPENDENCY_KEY_INFO_ID, DEPENDENCY_INFO_ID, XPATH_TO_KEY, SORT_ORDER, IS_INDICATOR) VALUES (11, 10, 'SIF_RefId/@SIF_RefObject', 1, 1);

-- -----------------------------------------------------
-- Data for table SCF_VALID_INDICATOR
-- -----------------------------------------------------
INSERT INTO SCF_VALID_INDICATOR (DEPENDENCY_KEY_INFO_ID, SIF_OBJECT_NAME) VALUES (11, 'StudentPersonal');
INSERT INTO SCF_VALID_INDICATOR (DEPENDENCY_KEY_INFO_ID, SIF_OBJECT_NAME) VALUES (11, 'StaffPersonal');
INSERT INTO SCF_VALID_INDICATOR (DEPENDENCY_KEY_INFO_ID, SIF_OBJECT_NAME) VALUES (11, 'StudentContactPersonal');

COMMIT;