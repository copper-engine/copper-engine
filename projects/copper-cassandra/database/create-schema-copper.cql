CREATE TABLE PNM_ORIGINAL_PERSON_DATA  (
   PERSON_NUMBER        varchar,
   RELATIONCOUNTER      TIMEUUID,
   LASTNAME             varchar,
   FIRSTNAME            varchar,
   SALUTATION           varchar,
   TITLE                varchar,
   BIRTHDAY             varchar,
   NATIONALITY          int,
   STREET               varchar,
   HOUSENUMBER          varchar,
   ADD_HOUSENUMBER      varchar,
   ZIPCODE              varchar,
   CITY                 varchar,
   COUNTRY              int,
   POSTBOX              varchar,
   IDNUMBERHASH         varchar,
   BANKCODE             varchar,
   BANKACCOUNT          varchar,
   PNM_IGNORE           boolean,
   BIC                  varchar,
   IBAN                 varchar,
   LASTNAME_KEY         varchar,
   FIRSTNAME_KEY        varchar,
   ADDRESS_KEY			varchar,
   INSERTED_AT          timestamp,
   primary key ((PERSON_NUMBER), RELATIONCOUNTER)
) with clustering order by (RELATIONCOUNTER desc);

create table PNM_SEARCH_AXIS (
   SEARCH_AXIS          varchar,
   SEARCH_KEY           varchar,
   OPD_PERSON_NUMBER    varchar,
   OPD_RELATIONCOUNTER  TIMEUUID,
   LASTNAME             varchar,
   FIRSTNAME            varchar,
   SALUTATION           varchar,
   TITLE                varchar,
   BIRTHDAY             varchar,
   NATIONALITY          int,
   STREET               varchar,
   HOUSENUMBER          varchar,
   ADD_HOUSENUMBER      varchar,
   ZIPCODE              varchar,
   CITY                 varchar,
   COUNTRY              int,
   POSTBOX              varchar,
   IDNUMBERHASH         varchar,
   BANKCODE             varchar,
   BANKACCOUNT          varchar,
   PNM_IGNORE           boolean,
   BIC                  varchar,
   IBAN                 varchar,   
   primary key ((SEARCH_AXIS, SEARCH_KEY), OPD_PERSON_NUMBER, OPD_RELATIONCOUNTER)
);

create table FOS_SCORES (
   BP_NUMBER   varchar,
   SCORES      map<int, decimal>,
   WRITE_TS	timestamp,
   primary key (BP_NUMBER)
);

create table RISK_LIST (
	TYPE        varchar,
	KEY         varchar,
	DESCRIPTION varchar,
	WRITE_TS	timestamp,
	primary key ((TYPE, KEY))
);

create table RISK_BONI_REQUEST (
	internal_request_id         uuid,
	external_request_id         varchar,
	external_system_id          varchar,
	external_callnumber         varchar,
	external_transactionnumber  varchar,
	geschaeftsfall              list<varchar>,
    bp_number                   varchar,
    person_number               varchar,
	request_json                varchar,
    result_json                 varchar,
	start_ts                    timestamp,
	elapsed_time_micro          bigint,
	primary key (internal_request_id)
);

create table IDX_RISK_REQUEST_EXTTXNID2INTID (
	external_transactionnumber  varchar,
	external_callnumber         varchar,
	internal_request_id         uuid,
    result_json                 varchar,
	primary key (external_transactionnumber, external_callnumber)
);

create table RISK_DECISION_HISTORY (
	internal_request_id  uuid,
	seqnr                timeuuid,
	start_ts             timestamp,
	elapsed_time_micro   bigint,
	decision_engine_id   varchar,
	decision_engine_rule varchar,
	input_xml            varchar,
	output_xml           varchar,
	input_json           varchar,
	output_json          varchar,
	primary key (internal_request_id, seqnr)
) with clustering order by (seqnr desc);
	
create table RISK_SOAP_HISTORY (
	internal_request_id  uuid,
	seqnr                timeuuid,
	start_ts             timestamp,
	elapsed_time_micro   bigint,
	system               varchar,
	endpoint             varchar,
	request              varchar,
	response             varchar,
	primary key (internal_request_id, seqnr)
) with clustering order by (seqnr desc);
	

