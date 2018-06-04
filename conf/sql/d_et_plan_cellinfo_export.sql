--GP导出 喻新杰
select create_if_not_exists('d_et_plan_cellinfo',
'CREATE TABLE "public"."d_et_plan_cellinfo" ( '
||' "city" varchar(100), '
||' "region" varchar, '
||' "gridid" text, '
||' "newsite_sn" varchar(40), '
||' "enodebid" text, '
||' "enodebname" varchar, '
||' "band" varchar(50), '
||' "sitetype" varchar, '
||' "covertype" varchar, '
||' "scenetype" varchar, '
||' "region_type" varchar(20), '
||' "status" varchar(20), '
||' "lonb" numeric(22,6), '
||' "latb" numeric(22,6), '
||' "opening_time" varchar, '
||' "record_update_time" timestamp, '
||' "cell1_direction" int8, '
||' "cell2_direction" int8, '
||' "cell3_direction" int8, '
||' "cell4_direction" int8, '
||' "cell5_direction" int8, '
||' "cell6_direction" int8, '
||' "cell1_tilt" int8, '
||' "cell2_tilt" int8, '
||' "cell3_tilt" int8, '
||' "cell4_tilt" int8, '
||' "cell5_tilt" int8, '
||' "cell6_tilt" int8, '
||' "cell1_ant_height" numeric, '
||' "cell2_ant_height" numeric, '
||' "cell3_ant_height" numeric, '
||' "cell4_ant_height" numeric, '
||' "cell5_ant_height" numeric, '
||' "cell6_ant_height" numeric '
||' ) '
||' WITH (OIDS=FALSE) '
||' ; '
||' ALTER TABLE "public"."d_et_plan_cellinfo" OWNER TO "ssmp" ');

drop EXTERNAL TABLE if EXISTS ext_mro1 ;

CREATE EXTERNAL TABLE ext_mro1 (
"city" varchar(100),
"region" varchar,
"gridid" text,
"newsite_sn" varchar(40),
"enodebid" text,
"enodebname" varchar,
"band" varchar(50),
"sitetype" varchar,
"covertype" varchar,
"scenetype" varchar,
"region_type" varchar(20),
"status" varchar(20),
"lonb" numeric(22,6),
"latb" numeric(22,6),
"opening_time" varchar,
"record_update_time" timestamp,
"cell1_direction" int8,
"cell2_direction" int8,
"cell3_direction" int8,
"cell4_direction" int8,
"cell5_direction" int8,
"cell6_direction" int8,
"cell1_tilt" int8,
"cell2_tilt" int8,
"cell3_tilt" int8,
"cell4_tilt" int8,
"cell5_tilt" int8,
"cell6_tilt" int8,
"cell1_ant_height" numeric,
"cell2_ant_height" numeric,
"cell3_ant_height" numeric,
"cell4_ant_height" numeric,
"cell5_ant_height" numeric,
"cell6_ant_height" numeric
) LOCATION (
'gpfdist://192.168.36.120:8081/d_et_plan_cellinfo/*.csv'
) FORMAT 'CSV' (DELIMITER ',' null 'null');
INSERT into d_et_plan_cellinfo select * from ext_mro1;
drop EXTERNAL table ext_mro1;


