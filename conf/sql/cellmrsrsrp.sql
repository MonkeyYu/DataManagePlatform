--小区MRSrsrp统计导出

--1.清除外部表
drop EXTERNAL table if EXISTS ext_edmp_cellmrsrsrp_${hiveconf:yyyyMM};
--2.创建外部表文件
$linux$touch /data1/load/yt_cellmrsrsrp_cell_${hiveconf:yyyyMM-1}.csv;
--3.创建外部表
CREATE WRITABLE EXTERNAL TABLE ext_edmp_cellmrsrsrp_${hiveconf:yyyyMM}(
		intyear int4,intmonth int4,vcname varchar,vccgi varchar,intrsrpcount INTEGER,intrsrpsum int8,fmrs110 float8 
	) location ('gpfdist://192.168.36.120:8081/yt_cellmrsrsrp_cell_${hiveconf:yyyyMM}.csv') format 'csv' (delimiter '|' null '');
--4.写入数据
insert into ext_edmp_cellmrsrsrp_${hiveconf:yyyyMM} 
select t.intYear,t.intmonth,t1.name,t.cgi vccgi,t.RSRPCOUNT,
RSRP07+RSRP08+RSRP09+RSRP10+RSRP11+RSRP12+RSRP13+RSRP14+RSRP15+RSRP16+RSRP17+RSRP18+RSRP19+RSRP20
+RSRP21+RSRP22+RSRP23+RSRP24+RSRP25+RSRP26+RSRP27+RSRP28+RSRP29+RSRP30+RSRP31+RSRP32+RSRP33+RSRP34
+RSRP35+RSRP36+RSRP37+RSRP38+RSRP39+RSRP40+RSRP41+RSRP42+RSRP43+RSRP44+RSRP45+RSRP46+RSRP47 as rsrpsum,
case when t.RSRPCOUNT=0 then 0 else 
trunc((RSRP07+RSRP08+RSRP09+RSRP10+RSRP11+RSRP12+RSRP13+RSRP14+RSRP15+RSRP16+RSRP17+RSRP18+RSRP19+RSRP20
+RSRP21+RSRP22+RSRP23+RSRP24+RSRP25+RSRP26+RSRP27+RSRP28+RSRP29+RSRP30+RSRP31+RSRP32+RSRP33+RSRP34
+RSRP35+RSRP36+RSRP37+RSRP38+RSRP39+RSRP40+RSRP41+RSRP42+RSRP43+RSRP44+RSRP45+RSRP46+RSRP47)/(t.RSRPCOUNT*1.0),5) end as mrs110
from yy_lte_rsrp_cell_month t 
left join (select vccgi,name from yy_ltesource_cell_month where intdateid=${hiveconf:yyyyMMdd-3} group by vccgi,name) t1 
on t.cgi=t1.vccgi where t.intyear=substr(${hiveconf:yyyyMM-1},0,5) and t.intmonth=${hiveconf:month}-1;
--5.删除外部表
drop EXTERNAL table if EXISTS ext_edmp_cellmrsrsrp_${hiveconf:yyyyMM};
--6.把导出文件移到指定目录
$linux$mv /data1/load/yt_cellmrsrsrp_cell_${hiveconf:yyyyMM-1}.csv /data1/ftp_data/yt_lte_cell_month/yt_cellmrsrsrp_cell_${hiveconf:yyyyMM-1}.csv;
--7.压缩文件，减少使用空间
$linux$gzip /data1/ftp_data/yt_lte_cell_month/yt_cellmrsrsrp_cell_${hiveconf:yyyyMM-7}.csv;
--8.月文件只保留6个月，需要清除6个月前的文件
$linux$rm -f /data1/ftp_data/yt_lte_cell_month/yt_cellmrsrsrp_cell_${hiveconf:yyyyMMdd-7}.csv;