--Mysql导出 喻新杰
select * from tbUser;

select ${hiveconf:yyyyMMdd-1},t2.vcAccount,t3.vcName,count(*) as "访问次数" from f_et_menu_visit_log t1 left join tbUser t2 on t1.intuserid=t2.bigId
left join tbPrivilege t3 ON t1.vcurl = t3.vcUrl
where concat(intyear,lpad(intmonth,2,0),lpad(intday,2,0)) = '${hiveconf:yyyyMMdd-1}'
group by vcName,vcAccount order by vcAccount ;