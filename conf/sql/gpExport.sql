--GP导出 喻新杰
select intcityid,vccity,intenbid,vcenbname,vcarea from ctbwkcellinfo_20171029 limit 210;

select ${hiveconf:yyyyMMdd-1},t1.vcorder_code,substr(t1.handler,position('/' in t1.handler)+1),t2.count2 as "初步分析次数",t3.count3 as "详细分析次数",t4.count4 as "效果评估次数" from(
select vcorder_code,handler from f_et_hzytb2 where handler is not null
and intyttime='${hiveconf:yyyyMMdd-1}' group by vcorder_code,handler
)t1 left join(
select vcorder_code,handler,sum(inthandlecount) count2 from f_et_hzytb2 where handler is not null
and vchandle_state='初步分析' and intyttime='${hiveconf:yyyyMMdd-1}' group by vcorder_code,handler
)t2 on t1.vcorder_code=t2.vcorder_code and t1.handler=t2.handler left join (
select vcorder_code,handler,sum(inthandlecount) count3 from f_et_hzytb2 where handler is not null
and vchandle_state='详细分析' and intyttime='${hiveconf:yyyyMMdd-1}' group by vcorder_code,handler
)t3 on t1.vcorder_code=t3.vcorder_code and t1.handler=t3.handler left join (
select vcorder_code,handler,sum(inthandlecount) count4 from f_et_hzytb2 where handler is not null
and vchandle_state='效果评估' and intyttime='${hiveconf:yyyyMMdd-1}' group by vcorder_code,handler
)t4 on t1.vcorder_code=t4.vcorder_code and t1.handler=t4.handler order by t1.vcorder_code,t1.handler;


