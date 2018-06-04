--深度覆盖 喻新杰
select func_depc_uemr_c_day('${hiveconf:yyyyMMdd-3}');
select func_depc_uemr_c_nc_day('${hiveconf:yyyyMMdd-3}');
select func_depc_uemr_g_c_day('${hiveconf:yyyyMMdd-3}');
select func_depc_uemr_g_c_nc_day('${hiveconf:yyyyMMdd-3}');
select func_depc_lonlat_day('${hiveconf:yyyyMMdd-3}');
select func_depc_lonlat_nc_day('${hiveconf:yyyyMMdd-3}');