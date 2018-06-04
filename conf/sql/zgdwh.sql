--子工单状态维护语句
drop table if EXISTS lte_cluster_question_state;
create table lte_cluster_question_state(
cluster_code varchar(200),
target varchar(200),
property varchar(200),
trim_village varchar(200),
detail_proposal_type varchar(200),
vcorderstate varchar(200),
vcordernumber varchar(200)
)with (appendonly=true,orientation=row) distributed randomly;
select setval('lte_cluster_question_state_seq',1,false);
insert into lte_cluster_question_state 
select cluster_code,target,property,trim_village,detail_proposal_type,vcorderstate,vcordernumber from (
select t.*,rank() OVER (PARTITION BY cluster_code ORDER BY rownum DESC)n1 from 
(select v.*,rank() OVER (PARTITION BY cluster_code ORDER BY ordernum DESC)n,nextval('lte_cluster_question_state_seq') rownum 
from lte_cluster_question_state_view v)t 
where n=1)x where n1 =1;
update lte_cluster_question c set order_state =s.vcorderstate,vcsonordercode=s.vcordernumber from lte_cluster_question_state s where s.cluster_code=c.cluster_code;
