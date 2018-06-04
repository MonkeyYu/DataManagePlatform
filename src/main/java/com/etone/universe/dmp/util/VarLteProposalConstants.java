package com.etone.universe.dmp.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 16-8-14
 * Time: 下午12:05
 * To change this template use File | Settings | File Templates.
 */
public class VarLteProposalConstants {

    public static int saveCount = 1000;

//    2G高流量宏站无LTE覆盖小区
    public static final String[] mt_2ggllhznolte_week = new String[] {"聚类问题点编号", "问题点编号", "时间", "地市", "区县", "所属BSC", "所属BTS", "CGI", "小区中文名", "小区英文名", "A网格ID", "B网格ID", "C网格ID", "覆盖类型", "覆盖场景", "价值标签", "经度", "纬度", "方向角", "数据流量", "最近的规划站距离", "是否已有规划站", "规划站名", "规划站经度", "规划站纬度", "最近的工程站距离", "是否已有工程站", "工程站名", "工程站经度", "工程站纬度", "优先处理（判断条件：已有规划站点加快建设及早入网）", "最近四周出现次数"};
//    2G高流量室分无LTE覆盖小区
    public static final String[] mt_2ggllsfnolte_week = new String[] {"聚类问题点编号", "问题点编号", "时间", "地市", "区县", "所属BSC", "所属BTS", "CGI", "小区中文名", "小区英文名", "A网格ID", "B网格ID", "C网格ID", "覆盖类型", "覆盖场景", "价值标签", "经度", "纬度", "方向角", "数据流量", "最近的规划站距离", "是否已有规划站", "规划站名", "规划站经度", "规划站纬度", "最近的工程站距离", "是否已有工程站", "工程站名", "工程站经度", "工程站纬度", "优先处理（判断条件：已有规划站点加快建设及早入网）", "最近四周出现次数"};
//    GSM高流量小区（一周）
    public static final String[] mt_gsmgllxq_week = new String[] {"聚类问题点编号", "问题点编号", "日期", "地市", "区县", "基站名", "所属BSC", "小区中文名", "小区英文名", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "工作频段", "覆盖类型", "覆盖场景", "设备厂商", "日均流量", "有流量天数", "高流量小区", "是否优先优化小区", "最近四周出现次数"};
//    高干扰小区
    public static final String[] mt_ggrxq_week = new String[] {"聚类问题点编号", "问题点编号", "时间", "地市", "区县", "所属ENODEB", "小区中文名", "小区英文名", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "工作频段", "覆盖类型", "覆盖场景", "价值标签", "设备厂商", "功率", "上行吞吐量MB", "下行吞吐量MB", "总流量MB", "小区带宽", "中心载频的信道号", "载频数量", "方位角", "最近两个月数据采集天数", "最近两个月出现高干扰频次", "最近一个月数据采集天数", "最近一个月出现高干扰频次", "干扰系数", "平均电平dBm", "是否优先优化小区", "是否本月新增", "小区RB上行平均干扰电平PRB0", "小区RB上行平均干扰电平PRB1", "小区RB上行平均干扰电平PRB2", "小区RB上行平均干扰电平PRB3", "小区RB上行平均干扰电平PRB4", "小区RB上行平均干扰电平PRB5", "小区RB上行平均干扰电平PRB6", "小区RB上行平均干扰电平PRB7", "小区RB上行平均干扰电平PRB8", "小区RB上行平均干扰电平PRB9", "小区RB上行平均干扰电平PRB10", "小区RB上行平均干扰电平PRB11", "小区RB上行平均干扰电平PRB12", "小区RB上行平均干扰电平PRB13", "小区RB上行平均干扰电平PRB14", "小区RB上行平均干扰电平PRB15", "小区RB上行平均干扰电平PRB16", "小区RB上行平均干扰电平PRB17", "小区RB上行平均干扰电平PRB18", "小区RB上行平均干扰电平PRB19", "小区RB上行平均干扰电平PRB20", "小区RB上行平均干扰电平PRB21", "小区RB上行平均干扰电平PRB22", "小区RB上行平均干扰电平PRB23", "小区RB上行平均干扰电平PRB24", "小区RB上行平均干扰电平PRB25", "小区RB上行平均干扰电平PRB26", "小区RB上行平均干扰电平PRB27", "小区RB上行平均干扰电平PRB28", "小区RB上行平均干扰电平PRB29", "小区RB上行平均干扰电平PRB30", "小区RB上行平均干扰电平PRB31", "小区RB上行平均干扰电平PRB32", "小区RB上行平均干扰电平PRB33", "小区RB上行平均干扰电平PRB34", "小区RB上行平均干扰电平PRB35", "小区RB上行平均干扰电平PRB36", "小区RB上行平均干扰电平PRB37", "小区RB上行平均干扰电平PRB38", "小区RB上行平均干扰电平PRB39", "小区RB上行平均干扰电平PRB40", "小区RB上行平均干扰电平PRB41", "小区RB上行平均干扰电平PRB42", "小区RB上行平均干扰电平PRB43", "小区RB上行平均干扰电平PRB44", "小区RB上行平均干扰电平PRB45", "小区RB上行平均干扰电平PRB46", "小区RB上行平均干扰电平PRB47", "小区RB上行平均干扰电平PRB48", "小区RB上行平均干扰电平PRB49", "小区RB上行平均干扰电平PRB50", "小区RB上行平均干扰电平PRB51", "小区RB上行平均干扰电平PRB52", "小区RB上行平均干扰电平PRB53", "小区RB上行平均干扰电平PRB54", "小区RB上行平均干扰电平PRB55", "小区RB上行平均干扰电平PRB56", "小区RB上行平均干扰电平PRB57", "小区RB上行平均干扰电平PRB58", "小区RB上行平均干扰电平PRB59", "小区RB上行平均干扰电平PRB60", "小区RB上行平均干扰电平PRB61", "小区RB上行平均干扰电平PRB62", "小区RB上行平均干扰电平PRB63", "小区RB上行平均干扰电平PRB64", "小区RB上行平均干扰电平PRB65", "小区RB上行平均干扰电平PRB66", "小区RB上行平均干扰电平PRB67", "小区RB上行平均干扰电平PRB68", "小区RB上行平均干扰电平PRB69", "小区RB上行平均干扰电平PRB70", "小区RB上行平均干扰电平PRB71", "小区RB上行平均干扰电平PRB72", "小区RB上行平均干扰电平PRB73", "小区RB上行平均干扰电平PRB74", "小区RB上行平均干扰电平PRB75", "小区RB上行平均干扰电平PRB76", "小区RB上行平均干扰电平PRB77", "小区RB上行平均干扰电平PRB78", "小区RB上行平均干扰电平PRB79", "小区RB上行平均干扰电平PRB80", "小区RB上行平均干扰电平PRB81", "小区RB上行平均干扰电平PRB82", "小区RB上行平均干扰电平PRB83", "小区RB上行平均干扰电平PRB84", "小区RB上行平均干扰电平PRB85", "小区RB上行平均干扰电平PRB86", "小区RB上行平均干扰电平PRB87", "小区RB上行平均干扰电平PRB88", "小区RB上行平均干扰电平PRB89", "小区RB上行平均干扰电平PRB90", "小区RB上行平均干扰电平PRB91", "小区RB上行平均干扰电平PRB92", "小区RB上行平均干扰电平PRB93", "小区RB上行平均干扰电平PRB94", "小区RB上行平均干扰电平PRB95", "小区RB上行平均干扰电平PRB96", "小区RB上行平均干扰电平PRB97", "小区RB上行平均干扰电平PRB98", "小区RB上行平均干扰电平PRB99"};
//    LTE超高站小区
    public static final String[] mt_ltecgxq_week = new String[] {"聚类问题点编号", "问题点编号", "日期", "地市", "区域", "所属ENODEB", "小区中文名", "小区英文名", "跟踪区码", "物理小区识别码", "本地小区标识", "频点", "CGI", "A网格ID", "B网格ID", "C网格ID", "载波数量", "覆盖类型", "覆盖场景", "厂家", "经度", "纬度", "工作频段", "电子下倾角", "机械下倾角", "总下倾角", "方向角", "天线挂高", "搜索半径(超远站)", "邻站个数(超远站)", "搜索半径(超近站)", "邻站个数(超近站)", "站间距_泰森多边形", "站间距_方向角(超远站)", "站间距_方向角(超近站)", "站间距(超远站)", "站间距(超近站)", "是否已有规划站", "泰森多边形邻区规划站", "方向角邻区规划站", "站间距_泰森多边形规划站", "站间距_方向角规划站", "最小站间距规划站", "是否已有工程站", "泰森多边形邻区工程站", "方向角邻区工程站", "站间距_泰森多边形工程站", "站间距_方向角工程站", "最小站间距工程站", "优先处理（已有规划站点加快建设及早入网）", "最近四周出现次数"};
//    LTE超近站小区
    public static final String[] mt_ltecjxq_week = new String[] {"聚类问题点编号", "问题点编号", "日期", "地市", "区域", "所属ENODEB", "小区中文名", "小区英文名", "跟踪区码", "物理小区识别码", "本地小区标识", "频点", "CGI", "A网格ID", "B网格ID", "C网格ID", "载波数量", "覆盖类型", "覆盖场景", "厂家", "经度", "纬度", "工作频段", "电子下倾角", "机械下倾角", "总下倾角", "方向角", "天线挂高", "搜索半径(超远站)", "邻站个数(超远站)", "搜索半径(超近站)", "邻站个数(超近站)", "站间距_泰森多边形", "站间距_方向角(超远站)", "站间距_方向角(超近站)", "站间距(超远站)", "站间距(超近站)", "是否已有规划站", "泰森多边形邻区规划站", "方向角邻区规划站", "站间距_泰森多边形规划站", "站间距_方向角规划站", "最小站间距规划站", "是否已有工程站", "泰森多边形邻区工程站", "方向角邻区工程站", "站间距_泰森多边形工程站", "站间距_方向角工程站", "最小站间距工程站", "优先处理（已有规划站点加快建设及早入网）", "最近四周出现次数"};
//    LTE超远站小区
    public static final String[] mt_ltecyxq_week = new String[] {"聚类问题点编号", "问题点编号", "日期", "地市", "区域", "所属ENODEB", "小区中文名", "小区英文名", "跟踪区码", "物理小区识别码", "本地小区标识", "频点", "CGI", "A网格ID", "B网格ID", "C网格ID", "载波数量", "覆盖类型", "覆盖场景", "价值标签", "厂家", "经度", "纬度", "工作频段", "电子下倾角", "机械下倾角", "总下倾角", "方向角", "天线挂高", "搜索半径(超远站)", "邻站个数(超远站)", "搜索半径(超近站)", "邻站个数(超近站)", "站间距_泰森多边形", "站间距_方向角(超远站)", "站间距_方向角(超近站)", "站间距(超远站)", "站间距(超近站)", "是否已有规划站", "泰森多边形邻区规划站", "方向角邻区规划站", "站间距_泰森多边形规划站", "站间距_方向角规划站", "最小站间距规划站", "是否已有工程站", "泰森多边形邻区工程站", "方向角邻区工程站", "站间距_泰森多边形工程站", "站间距_方向角工程站", "最小站间距工程站", "优先处理（已有规划站点加快建设及早入网）", "最近四周出现次数"};
//    LTE高重叠覆盖路段概要信息
    public static final String[] mt_cdfggy_cell = new String[] {"聚类问题点编号", "问题点编号", "地市", "网格名称", "道路名称", "连续栅格集", "连续栅格数", "最大重叠覆盖度", "最小重叠覆盖度", "平均重叠覆盖度"};
//    LTE高重叠覆盖路段详细信息
    public static final String[] mt_cdfgxx_cell = new String[] {"聚类问题点编号", "问题点编号", "地市", "网格名称", "中心经度", "中心纬度", "道路名称", "连续栅格集", "连续栅格数", "最大重叠覆盖度", "最小重叠覆盖度", "平均重叠覆盖度", "栅格编号", "采样点数", "RSRP平均值", "重叠覆盖度", "小区名", "EARFCN", "PCI", "采样点数", "RSRP平均值", "剔除情况"};
//    高负荷待扩容小区（新算法）
    public static final String[] mt_gfhdhrxq_week = new String[] {"聚类问题点编号", "问题点编号", "时间", "地市", "区县", "小区英文名", "小区中文名", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "覆盖类型", "覆盖场景", "价值标签", "厂家名称", "工作频段", "功率", "上行吞吐量(MB)", "下行吞吐量(MB)", "自忙时平均E-RAB流量", "上行PUSCH PRB占用平均数", "上行PUSCH PRB可用平均数", "上行PRB平均利用率%", "下行PDSCH PRB占用平均数", "下行PDSCH PRB可用平均数", "下行PRB平均利用率%", "PDCCH信道CCE占用率", "RRC连接平均数", "RRC连接最大数", "有效RRC连接平均数", "有效RRC连接最大数", "RRC连接建立请求次数", "RRC连接建立成功次数", "RRC连接建立成功率", "E-RAB建立请求数", "E-RAB建立成功数", "E-RAB建立成功率", "无线接通率", "无线掉线率", "切换成功率", "小区故障类型", "扩容门限", "最近四周出现次数"};
//    LTE上传低速率路段(<=512K),LTE下载低速率路段(<=10M),LTE下载低速率路段(<=2M)
    public static final String[] mt_yyscxz_cell = new String[] {"聚类问题点编号", "问题点编号", "地市", "网格名称", "文件名", "开始时间", "中心经度", "中心纬度", "道路", "业务类型", "网络类型", "持续距离(米)", "持续时间(秒)", "采样点总数", "低速率采样点个数", "低速率采样点占比(%)", "小于2M采样点个数", "小于2M采样点占比（%）", "小于512K采样点个数", "小于512K采样点占比", "大于等于1M采样点个数", "大于等于1M采样点占比", "最高速率(M)", "最低速率(M)", "0速率里程", "0速率里程占比(%)", "平均速率(M)", "PDSCH_BLER平均值", "RSRP最大值", "RSRP最小值", "RSRP平均值", "邻区RSRP最大值", "邻区RSRP最小值", "邻区RSRP平均值", "SINR最大值", "SINR最小值", "SINR平均值", "连续SINR质差里程占比(%)", "重叠覆盖度≥3比例", "重叠覆盖里程占比", "下行码字0MCS平均值", "下行码字1MCS平均值", "下行码字0最高频率MCS(%)", "下行码字1最高频率MCS(%)", "码字0CQI平均值", "码字1CQI平均值", "下行码字0 64QAM占比", "下行码字1 64QAM占比", "下行码字0 16QAM占比", "下行码字1 16QAM占比", "Throughput_DL最大值", "Throughput_DL最小值", "Throughput_DL平均值", "Transmission_Mode", "TM3比例（%）", "rank_indicator", "双流时长占比（%）", "误块率（%）", "PDSCH_RB_Number", "PRB调度数", "PDCCH_DL_Grant_Count", "Ratio_DL_Code0_HARQ_ACK", "Ratio_DL_Code0_HARQ_NACK", "Ratio_DL_Code1_HARQ_ACK", "Ratio_DL_Code1_HARQ_NACK"};
//    CSFB事件列表
    public static final String[] mt_csfbcgl_cell = new String[] {"聚类问题点编号", "问题点编号", "地市", "网格名称", "文件名", "事件发生时间", "中心经度", "中心纬度", "异常事件名称", "TAC", "ECI"};
//    GSM连续质差路段
    public static final String[] mt_gsmlxcld_cell = new String[] {"聚类问题点编号", "问题点编号", "地市", "网格名称", "文件名", "开始时间", "中心经度", "中心纬度", "道路", "持续距离(米)", "持续时间(秒)", "采样点个数", "最大RxQuality", "最小RxQuality", "平均RxQuality", "最大场强", "最小场强", "平均场强", "最大C/I", "最小C/I", "平均C/I", "占用小区", "占用小区名"};
//    VOLTE_RTP丢包事件
    public static final String[] mt_voltertodbsj_week = new String[] {"聚类问题点编号", "问题点编号", "地市", "网格名称", "文件名", "事件发生时间", "中心经度", "中心纬度", "异常事件名称", "TAC", "ECI", "持续距离"};
//    VOLTE掉话事件
    public static final String[] mt_voltedhsj_week = new String[] {"聚类问题点编号", "问题点编号", "地市", "网格名称", "文件名", "事件发生时间", "中心经度", "中心纬度", "异常事件名称", "TAC", "ECI"};
//    VOLTE_持续弱MOS事件
    public static final String[] mt_voltecxrmossj_week = new String[] {"聚类问题点编号", "问题点编号", "地市", "网格名称", "文件名", "事件发生时间", "中心经度", "中心纬度", "异常事件名称", "TAC", "ECI", "持续距离"};
//    VOLTE未接通事件
    public static final String[] mt_voltenoconnection_week = new String[] {"聚类问题点编号", "问题点编号", "地市", "网格名称", "文件名", "事件发生时间", "中心经度", "中心纬度", "异常事件名称", "TAC", "ECI"};
//    LTE弱覆盖路段(RSRP≤-100),LTE弱覆盖路段(RSRP≤-110)
    public static final String[] mt_rfg_cell = new String[] {"聚类问题点编号", "问题点编号", "地市", "网格名称", "文件名", "开始时间", "中心经度", "中心纬度", "道路", "弱覆盖点占比(%)", "持续距离(米)", "持续时间(秒)", "采样点个数", "RSRP最大值", "RSRP最小值", "RSRP平均值", "最强邻区最大RSRP", "最强邻区最小RSRP", "最强邻区平均RSRP", "最大SINR值", "最小SINR值", "平均SINR值"};
//    LTE连续质差路段(SINR≤-3dB),LTE连续质差路段(SINR≤0dB)
    public static final String[] mt_lxzc_cell = new String[] {"聚类问题点编号", "问题点编号", "地市", "网格名称", "文件名", "开始时间", "中心经度", "中心纬度", "道路", "质差点占比(%)", "持续距离(米)", "持续时间(秒)", "采样点个数", "最大SINR值", "最小SINR值", "平均SINR值", "RSRP最大值", "RSRP最小值", "RSRP平均值", "平均下载速率(Mbps)", "下行码字0 64QAM占比（%）", "双流时长占比(%)", "误块率（%）", "TM3比例（%）", "PRB调度数", "下行码字1 64QAM占比（%）"};
//    eSVRCC切换差小区
    public static final String[] mt_esvrccqhcxq_week = new String[] {"聚类问题点编号", "问题点编号", "地市", "区县", "所属ENODEB", "小区英文名", "小区中文名", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "工作频段", "覆盖类型", "覆盖场景", "价值标签", "设备厂商", "功率", "2017-07-05", "2017-07-06", "2017-07-07", "2017-07-08", "2017-07-09", "2017-07-10", "2017-07-11", "有指标天数", "一周出现大于2次低于目标值", "切换至2G请求次数", "切换至2G成功次数", "是否优先优化小区", "最近四周出现次数"};
//    VoLTE E-RAB掉线高小区(视频)
    public static final String[] mt_volteerabdxgxqsp_week = new String[] {"聚类问题点编号", "问题点编号", "地市", "区县", "所属ENODEB", "小区英文名", "小区中文名", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "工作频段", "覆盖类型", "覆盖场景", "价值标签", "设备厂商", "功率", "2017-07-05", "2017-07-06", "2017-07-07", "2017-07-08", "2017-07-09", "2017-07-10", "2017-07-11", "有指标天数", "一周出现大于2次低于目标值", "QCI2的E-RAB建立请求数", "QCI2的E-RAB建立成功数", "eNB请求释放的E-RAB数-2", "正常的eNB请求释放的E-RAB数-2", "切出失败的E-RAB数-QCI-2", "遗留E-RAB个数-业务类型2", "QCI1切换入E-RAB数", "是否优先优化小区", "最近四周出现次数"};
//    VoLTE E-RAB掉线高小区(语音)
    public static final String[] mt_volteerabdxgxqyy_week = new String[] {"聚类问题点编号", "问题点编号", "地市", "区县", "所属ENODEB", "小区英文名", "小区中文名", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "工作频段", "覆盖类型", "覆盖场景", "价值标签", "设备厂商", "功率", "2017-07-05", "2017-07-06", "2017-07-07", "2017-07-08", "2017-07-09", "2017-07-10", "2017-07-11", "有指标天数", "一周出现大于2次低于目标值", "QCI1的E-RAB建立请求数", "QCI1的E-RAB建立成功数", "eNB请求释放的E-RAB数-1", "正常的eNB请求释放的E-RAB数-1", "切出失败的E-RAB数-QCI-1", "遗留E-RAB个数-业务类型1", "QCI1切换入E-RAB数", "是否优先优化小区", "最近四周出现次数"};
//    VOLTE_eSRVCC切换失败事件
    public static final String[] mt_volteesrvccqhsbsj_week = new String[] {"聚类问题点编号", "问题点编号", "地市", "网格名称", "文件名", "事件发生时间", "中心经度", "中心纬度", "异常事件名称", "TAC", "ECI"};
//    VoLTE无线接通差小区(视频)
    public static final String[] mt_voltewxjtcxqsp_week = new String[] {"聚类问题点编号", "问题点编号", "地市", "区县", "所属ENODEB", "小区英文名", "小区中文名", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "工作频段", "覆盖类型", "覆盖场景", "价值标签", "设备厂商", "功率", "2017-07-05", "2017-07-06", "2017-07-07", "2017-07-08", "2017-07-09", "2017-07-10", "2017-07-11", "有指标天数", "一周出现大于2次低于目标值", "QCI2的E-RAB建立请求数", "QCI2的E-RAB建立成功数", "RRC连接建立成功次数", "RRC连接建立请求次数", "是否优先优化小区", "最近四周出现次数"};
//    VoLTE无线接通差小区(语音)
    public static final String[] mt_voltewxjtcxqyy_week = new String[] {"聚类问题点编号", "问题点编号", "地市", "区县", "所属ENODEB", "小区英文名", "小区中文名", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "工作频段", "覆盖类型", "覆盖场景", "价值标签", "设备厂商", "功率", "2017-07-05", "2017-07-06", "2017-07-07", "2017-07-08", "2017-07-09", "2017-07-10", "2017-07-11", "有指标天数", "一周出现大于2次低于目标值", "QCI1的E-RAB建立请求数", "QCI1的E-RAB建立成功数", "RRC连接建立成功次数", "RRC连接建立请求次数", "是否优先优化小区", "最近四周出现次数"};
//    VoLTE下行高时延小区
    public static final String[] mt_voltexxgsyxq_week = new String[] {"聚类问题点编号", "问题点编号", "地市", "区县", "所属ENODEB", "小区英文名", "小区中文名", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "工作频段", "覆盖类型", "覆盖场景", "价值标签", "设备厂商", "功率", "2017-07-05", "2017-07-06", "2017-07-07", "2017-07-08", "2017-07-09", "2017-07-10", "2017-07-11", "有指标天数", "一周出现大于2次低于目标值", "VoLTE下行平均时延之平均值", "是否优先优化小区", "最近四周出现次数"};
//    LTE MR弱覆盖小区
    public static final String[] mt_ltemrrfgxq_week = new String[] {"聚类问题点编号", "问题点编号", "地市", "时间", "区县", "厂家", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "小区中文名", "小区英文名", "覆盖类型", "覆盖场景", "价值标签", "频段", "中心频点", "MRO总采样点数", "MRO大于等于负110DBM的采样点数", "MRO覆盖率大于等于负110DBM", "MRO小于负110DBM的采样点", "优先处理（低于门限且采样点贡献大）", "是否本月新增加", "全省MR弱覆盖小区整治清单标识"};
//    全网劣于竞争对手小区
    public static final String[] mt_qwlyjzdsxq_week = new String[] {"聚类问题点编号", "问题点编号", "地市", "日期", "区县", "所属ENODEB", "小区中文名", "小区英文名", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "工作频段", "覆盖类型", "覆盖场景", "价值标签", "设备厂商", "小区状态", "移动总采样点数", "移动小于-110采样点数", "移动覆盖率（-110）", "联通总采样点数", "联通小于-113采样点数", "联通覆盖率（-113）", "电信总采样点数", "电信小于-113采样点数", "电信覆盖率（-113）", "是否劣于联通", "是否劣于电信", "是否劣于联通电信", "是否本月新增", "是否优先处理"};
//    GSM无线掉话率（一周出现大于3次低于目标值）小区
    public static final String[] mt_gsmwxdhl_week = new String[] {"聚类问题点编号", "问题点编号", "地市", "区县", "所属BTS", "所属BSC", "小区中文名", "小区英文名", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "工作频段", "覆盖类型", "覆盖场景", "设备厂商", "2016-07-20", "2016-07-21", "2016-07-22", "2016-07-23", "2016-07-24", "2016-07-25", "2016-07-26", "周指标", "一周出现不达标天数总计", "有指标天数", "每线话务量（ERL)【有指标天数日均值】", "话音信道掉话总次数【有指标天数日均值】", "话音信道占用总次数【有指标天数日均值】", "GSM无线掉话次数贡献量", "一周出现大于3次低于目标值", "是否优先优化小区", "最近四周出现次数"};
//    GSM无线接通率（一周出现大于3次低于目标值）小区
    public static final String[] mt_gsmwxjtl_week = new String[] {"聚类问题点编号", "问题点编号", "地市", "区县", "所属BTS", "所属BSC", "小区中文名", "小区英文名", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "工作频段", "覆盖类型", "覆盖场景", "设备厂商", "2016-07-20", "2016-07-21", "2016-07-22", "2016-07-23", "2016-07-24", "2016-07-25", "2016-07-26", "周指标", "一周出现不达标天数总计", "有指标天数", "每线话务量（ERL)【有指标天数日均值】", "SDCCH分配次数【有指标天数日均值】", "话音信道占用次数【有指标天数日均值】", "SDCCH试呼次数【有指标天数日均值】", "话音信道试呼次数【有指标天数日均值】", "无线接通失败次数", "一周出现大于3次低于目标值", "是否优先优化小区", "最近四周出现次数"};
//    GSM质差话务比例
    public static final String[] mt_gsmzchwbl_week = new String[] {"聚类问题点编号", "问题点编号", "地市", "区县", "所属BTS", "所属BSC", "小区中文名", "小区英文名", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "工作频段", "覆盖类型", "覆盖场景", "设备厂商", "2016-07-20", "2016-07-21", "2016-07-22", "2016-07-23", "2016-07-24", "2016-07-25", "2016-07-26", "周均值", "一周出现不达标天数总计", "有指标天数", "上下行Rxquality 6、7级的采样点【有指标天数日均值】", "上下行采样点总数【有指标天数日均值】", "GSM质差话务采样点贡献量", "是否优先优化小区", "最近四周出现次数", "早一周指标", "早两周指标", "早三周指标"};
//    LTE掉线高小区
    public static final String[] mt_ltewxdxl_week = new String[] {"聚类问题点编号", "问题点编号", "地市", "区县", "所属ENODEB", "小区中文名", "小区英文名", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "工作频段", "覆盖类型", "覆盖场景", "价值标签", "设备厂商", "2017-07-05", "2017-07-06", "2017-07-07", "2017-07-08", "2017-07-09", "2017-07-10", "2017-07-11", "周指标", "一周出现不达标天数总计", "有指标天数", "吞吐量（MB)【有指标天数日均值】", "eNB请求释放上下文数【有指标天数日均值】", "正常的eNB请求释放上下文数【有指标天数日均值】", "初始上下文建立成功次数【有指标天数日均值】", "遗留上下文个数", "LTE无线掉线次数贡献量", "一周出现大于等于3次低于目标值", "是否优先优化小区", "最近四周出现次数"};
//    LTE接通低小区
    public static final String[] mt_ltewxjtl_week = new String[] {"聚类问题点编号", "问题点编号", "地市", "区县", "所属ENODEB", "小区中文名", "小区英文名", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "工作频段", "覆盖类型", "覆盖场景", "价值标签", "设备厂商", "2017-07-05", "2017-07-06", "2017-07-07", "2017-07-08", "2017-07-09", "2017-07-10", "2017-07-11", "周指标", "一周出现不达标天数总计", "有指标天数", "吞吐量（MB)【有指标天数日均值】", "RRC连接建立成功次数【有指标天数日均值】", "RRC连接建立请求次数【有指标天数日均值】", "E-RAB建立成功数【有指标天数日均值】", "E-RAB建立请求数", "无线接通失败次数", "一周出现大于等于3次低于目标值", "是否优先优化小区", "最近四周出现次数"};
    //    LTE切换差小区
    public static final String[] mt_lteqhcxq_week = new String[] {"聚类问题点编号", "问题点编号", "地市", "区县", "所属ENODEB", "小区中文名", "小区英文名", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "工作频段", "覆盖类型", "覆盖场景", "价值标签", "设备厂商", "功率", "2017-07-05", "2017-07-06", "2017-07-07", "2017-07-08", "2017-07-09", "2017-07-10", "2017-07-11", "周指标", "一周出现不达标天数总计", "有指标天数", "eNB间S1切换出成功次数【有指标天数日均值】", "eNB间X2切换出成功次数【有指标天数日均值】", "eNB内切换出成功次数【有指标天数日均值】", "eNB间S1切换出请求次数【有指标天数日均值】", "eNB间X2切换出请求次数【有指标天数日均值】", "eNB内切换出请求次数【有指标天数日均值】", "切换成功次数【有指标天数日均值】", "切换请求次数【有指标天数日均值】", "上行吞吐量MB【有指标天数日均值】", "下行吞吐量MB【有指标天数日均值】", "总流量MB【有指标天数日均值】", "一周出现大于等于3次低于目标值", "是否优先优化小区", "最近四周出现次数"};
    //    高流量问题严重小区
    public static final String[] mt_4ggllwtyzxq_week = new String[] {"聚类问题点编号", "问题点编号", "地市", "区县", "所属ENODEB", "小区中文名", "小区英文名", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "设备厂商", "是否关联状态库", "站点状态", "工作频段", "覆盖类型", "覆盖场景", "价值标签", "告警次数", "日流量（GB）", "日流量增长系数", "最大有效RRC连接数", "最大有效RRC连接数增长系数", "最大RRC连接数", "最大RRC连接数增长系数", "日峰值下行PRB利用率", "下行PRB利用率增长系数", "日峰值上行PRB利用率", "上行PRB利用率增长系数", "日峰值PDCCH信道占用率", "PDCCH信道CCE占用率增长系数", "高流量预警标签", "最近四周出现次数"};
    //    倒流小区明细
    public static final String[] mt_dlxqmx_week = new String[] {"聚类问题点编号", "问题点编号", "时间", "地市", "区县", "网络制式", "小区CGI", "小区中文名", "小区英文名", "A网格ID", "B网格ID", "C网格ID", "区域类型", "覆盖场景", "价值标签", "经度", "纬度", "方向角", "基站高倒流小区数", "倒流流量(MB)", "4G终端数", "共覆盖4G基站ID", "共覆盖4G基站名称", "4G基站经度", "4G基站纬度", "4G基站状态", "所属规划期", "4G物理站小区平均话单流量(MB)", "与4G基站站间距(米)", "倒流比例", "属于周粒度倒流总流量最多的约前10%的网格(是/否)", "最近4周出现次数"};
    //    信令道路覆盖问题点
    public static final String[] yt_xldlfg_grid = new String[] {"聚类问题点编号", "问题点编号","地市名称","问题点经度","问题点纬度","时间","A类网格","B类网格","C类网格","道路名称","问题类别","道路栅格ID","栅格采样点总数","栅格内RSRP的平均值","栅格内RSRP大于-110dBm采样点的比例","栅格内RSRP小于-110dBm的采样点数","采样点最多小区CGI号码","采样点最多小区号码","采样点最多小区名称","栅格内主覆盖小区采样点总数","栅格内主覆盖小区RSRP的平均值","弱覆盖采样点最多的小区号","弱覆盖采样点最多的小区号称","栅格内问题小区RSRP大于-110dBm采样点的比例","栅格内问题小区RSRP小于-110dBm的采样点数","栅格内问题小区的平均TA取值","栅格内问题小区的平均PHR取值","弱覆盖采样点中，邻小区也是弱信号或未测量到邻小区的这类处于覆盖边缘的弱覆盖采样点的占比","在主小区RSRP大于-110dBm的采样点中，邻小区跟主小区同频、邻小区RSRP和主小区RSRP差大于-6dB且满足以上条件的邻小区数目大于等于3的样本点占比"};
    //信令道路干扰问题点
    public static final String[] yt_xldlgr_grid = new String[] {"聚类问题点编号", "问题点编号","地市名称","问题点经度","问题点纬度","时间","A类网格","B类网格","C类网格","道路名称","问题类别","道路栅格ID","栅格采样点总数","栅格内RSRP的平均值","栅格内RSRP大于-110dBm采样点的比例","栅格内RSRP小于-110dBm的采样点数","栅格内模三干扰的平均值","栅格内模三干扰采样点数","采样点最多小区CGI号码","采样点最多小区号码","采样点最多小区名称","栅格内主覆盖小区采样点总数","栅格内主覆盖小区RSRP的平均值","模三干扰采样点最多的小区号","模三干扰采样点最多的小区号称","栅格内问题小区模三干扰的平均值","问题小区模三干扰采样点数","问题小区的PCI","栅格内模三相等的小区PCI列表","栅格内干扰小区RSRP的平均值","栅格内问题小区RSRP的平均值","栅格内问题小区RSRP大于-110dBm采样点的比例","栅格内问题小区的平均TA取值","栅格内问题小区的平均PHR取值","弱覆盖采样点中，邻小区也是弱信号或未测量到邻小区的这类处于覆盖边缘的弱覆盖采样点的占比","在主小区RSRP大于-110dBm的采样点中，邻小区跟主小区同频、邻小区RSRP和主小区RSRP差大于-6dB且满足以上条件的邻小区数目大于等于3的样本点占比"};
    //VOLTE用户切换差小区
    public static final String[] mt_volteyhqhcxq_cell = new String[] {"聚类问题点编号","问题点编号", "地市", "区县", "所属ENODEB", "小区英文名", "小区中文名", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "工作频段", "覆盖类型", "覆盖场景", "价值标签", "设备厂商", "功率", "2017-07-05", "2017-07-06", "2017-07-07", "2017-07-08", "2017-07-09", "2017-07-10", "2017-07-11", "有指标天数", "一周出现大于2次低于目标值", "VoLTE用户eNB间S1切换出成功次数", "VoLTE用户eNB间S1切换出请求次数", "VoLTE用户eNB间X2切换出成功次数", "VoLTE用户eNB间X2切换出请求次数", "VoLTE用户eNB内切换出成功次数", "VoLTE用户eNB内切换出请求次数", "是否优先优化小区", "最近四周出现次数"};
    //QCI2承载切换差小区
    public static final String[] mt_qci2czqhcxq_cell = new String[] {"聚类问题点编号", "问题点编号", "地市", "区县", "所属ENODEB", "小区英文名", "小区中文名", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "工作频段", "覆盖类型", "覆盖场景", "价值标签", "设备厂商", "功率", "2017-07-05", "2017-07-06", "2017-07-07", "2017-07-08", "2017-07-09", "2017-07-10", "2017-07-11", "有指标天数", "一周出现大于2次低于目标值", "QCI2承载eNB间S1切换出成功次数", "QCI2承载eNB间S1切换出请求次数", "QCI2承载eNB间X2切换出成功次数", "QCI2承载eNB间X2切换出请求次数", "QCI2承载eNB内切换出成功次数", "QCI2承载eNB内切换出请求次数", "是否优先优化小区", "最近四周出现次数"};
    //volte_VOLTE_IMS注册失败事件
    public static final String[] mt_volteimszcsbsj_cell = new String[] {"聚类问题点编号", "问题点编号","地市","网格名称","文件名","事件发生时间","中心经度","中心纬度","异常事件名称","TAC","ECI"};
    //volte_esrvcc切换时延-用户面（ms）
    public static final String[] mt_volteesrvccqhsyyhm_cell = new String[] {"聚类问题点编号","问题点编号","地市","网格名称","文件名","事件发生时间","中心经度","中心纬度","异常事件名称","TAC","ECI"};
    //VOLTE呼叫建立时延
    public static final String[] mt_voltehjjlsy_cell = new String[] {"聚类问题点编号", "问题点编号","地市","网格名称","文件名","事件发生时间","中心经度","中心纬度","异常事件名称","TAC","ECI"};
    // 投诉4G弱覆盖小区整治
    public static final String[] mt_ts4grfgxqzz_cell = new String[]{"问题点编号","地市","日期","主要活动LTE小区1","主要活动LTE小区CGI","经度","纬度","主要活动LTE小区MR覆盖率","覆盖场景","覆盖类型","备注"};
    //投诉补4G覆盖
    public static final String[] mt_tsb4gfg_cell =new String[]{"问题点编号","投诉日期","地市","主要活动小区1","主要活动小区1的小区CGI","经度","纬度","问题原因，需补4G覆盖","备注"};
    //VoLTE上行高丢包
    public static final String[] mt_voltesxgdbxq_cell =new String[]{"问题点编号", "地市", "区县", "所属ENODEB", "小区英文名", "小区中文名", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "工作频段", "覆盖类型", "覆盖场景", "价值标签", "设备厂商", "功率", "2017-07-05", "2017-07-06", "2017-07-07", "2017-07-08", "2017-07-09", "2017-07-10", "2017-07-11", "有指标天数", "一周出现大于2次低于目标值", "是否优先优化小区", "最近四周出现次数"};
    //Volte下行高丢包
    public static final String[] mt_voltexxgdbxq_cell =new String[]{"问题点编号", "地市", "区县", "所属ENODEB", "小区英文名", "小区中文名", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "工作频段", "覆盖类型", "覆盖场景", "价值标签", "设备厂商", "功率", "2017-07-05", "2017-07-06", "2017-07-07", "2017-07-08", "2017-07-09", "2017-07-10", "2017-07-11", "有指标天数", "一周出现大于2次低于目标值", "是否优先优化小区", "最近四周出现次数"};
    //20170715新增问题点-同频高重叠覆盖小区
    public static final String[] mt_tpgcdfgxq_cell =new String[]{"聚类问题点编号", "问题点编号", "地市", "时间", "区县", "厂家", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "小区中文名", "小区英文名", "覆盖类型", "覆盖场景", "价值标签", "频段", "中心频点", "DB6内3邻区采样数", "DB6内3邻区采样比例", "同频DB6内3邻区采样数", "同频DB6内3邻区采样比例", "同频DB6内1邻区采样数", "同频DB6内1邻区采样比例", "优先处理（低于门限且采样点贡献大）", "是否本月新增加"};
    //20170830新增问题点-LTE零流量小区
    public static final String[] mt_ltelllxq_cell =new String[]{"聚类问题点编号", "问题点编号", "时间", "地市", "区县", "所属ENODEB", "小区中文名", "小区英文名", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "工作频段", "覆盖类型", "覆盖场景", "价值标签", "设备厂商", "功率", "运行状态", "频点号", "PCI", "总下倾角", "方向角", "上行吞吐量（MB）", "下行吞吐量（MB）", "总吞吐量（MB）", "统计天数", "有流量天数", "最近四周出现次数"};

    //20171009新增问题点-用户投诉问题点
    public static final String[] yy_complainproblem =new String[]{"聚类问题点编号", "问题点编号", "时间", "地市", "区县", "所属ENODEB", "小区中文名", "小区英文名", "CGI", "A网格ID", "B网格ID", "C网格ID", "经度", "纬度", "工作频段", "覆盖类型", "覆盖场景", "价值标签", "设备厂商", "功率", "运行状态", "频点号", "PCI", "总下倾角", "方向角", "上行吞吐量（MB）", "下行吞吐量（MB）", "总吞吐量（MB）", "统计天数", "有流量天数", "最近四周出现次数"};

    //    派单附件sheet名称
    public static final String[] mt_sheet_name = new String[] {"2G高流量宏站无LTE覆盖小区", "2G高流量室分无LTE覆盖小区"/*, "GSM高流量小区（一周）"*/, "高干扰小区", /*"LTE超高站小区","LTE超近站小区", "LTE超远站小区", "同频高重叠覆盖小区", "LTE高重叠覆盖路段概要信息", "LTE高重叠覆盖路段详细信息", "高校高负荷小区","高负荷待扩容小区（新算法）","LTE上传低速率路段(<=512K)",*/ "LTE下载低速率路段(<=10M)", /*"LTE下载低速率路段(<=2M)", "LTE上传低速率路段(小于等于512K)",*/ "LTE下载低速率路段(小于等于10M)", /*"LTE下载低速率路段(小于等于2M)",*/ "CSFB事件列表", /*"GSM连续质差路段",*/ "VOLTE_RTP丢包事件", "VOLTE掉话事件", "VOLTE_持续弱MOS事件", "VOLTE未接通事件", "LTE连续质差路段(SINR≤-3dB)", /*"LTE弱覆盖路段(RSRP≤-100)",*/ "LTE弱覆盖路段(RSRP≤-110)", /*"LTE连续质差路段(SINR≤0dB)",*/ "LTE连续质差路段(SINR小于等于-3dB)", "LTE弱覆盖路段(RSRP小于等于-100)", "LTE弱覆盖路段(RSRP小于等于-110)", "LTE连续质差路段(SINR小于等于0dB)", "eSVRCC切换差小区", /*"VoLTE E-RAB掉线高小区(视频)",*/ "VoLTE E-RAB掉线高小区(语音)", "VOLTE_eSRVCC切换失败事件", /*"VoLTE无线接通差小区(视频)",*/ "VoLTE无线接通差小区(语音)", "VoLTE下行高时延小区", "LTE MR弱覆盖小区", "全网劣于竞争对手小区",/*"GSM无线掉话率（一周出现大于3次低于目标值）小区",*/ /*"GSM无线接通率（一周出现大于3次低于目标值）小区",*/ /*"GSM质差话务比例",*/ "LTE掉线高小区", "LTE接通低小区","LTE切换差小区","高流量问题严重小区","LTE零流量小区", "倒流小区明细","信令道路干扰问题点","信令道路覆盖问题点","VOLTE用户切换差小区",/*"QCI2承载切换差小区",*//*"VOLTE_IMS注册失败事件","VOLTE_eSRVCC切换时延-用户面(ms)","VOLTE呼叫建立时延",*//*"投诉4G弱覆盖小区整治","投诉补4G覆盖",*/"VoLTE上行高丢包小区","VoLTE下行高丢包小区","用户投诉"};

    public static final Map<String, String[]> SHEET_TITLE_MAP = new HashMap<String, String[]>();
    static {
        SHEET_TITLE_MAP.put("2G高流量宏站无LTE覆盖小区", mt_2ggllhznolte_week);
        SHEET_TITLE_MAP.put("2G高流量室分无LTE覆盖小区", mt_2ggllsfnolte_week);
        SHEET_TITLE_MAP.put("GSM高流量小区（一周）", mt_gsmgllxq_week);
        SHEET_TITLE_MAP.put("高干扰小区", mt_ggrxq_week);
        SHEET_TITLE_MAP.put("LTE超高站小区", mt_ltecgxq_week);
        SHEET_TITLE_MAP.put("LTE超近站小区", mt_ltecjxq_week);
        SHEET_TITLE_MAP.put("LTE超远站小区", mt_ltecyxq_week);
        SHEET_TITLE_MAP.put("同频高重叠覆盖小区", mt_tpgcdfgxq_cell);
        SHEET_TITLE_MAP.put("LTE高重叠覆盖路段概要信息", mt_cdfggy_cell);
        SHEET_TITLE_MAP.put("LTE高重叠覆盖路段详细信息", mt_cdfgxx_cell);
        SHEET_TITLE_MAP.put("高负荷待扩容小区（新算法）", mt_gfhdhrxq_week);
        SHEET_TITLE_MAP.put("高校高负荷小区", mt_gfhdhrxq_week);
        SHEET_TITLE_MAP.put("LTE上传低速率路段(<=512K)", mt_yyscxz_cell);
        SHEET_TITLE_MAP.put("LTE下载低速率路段(<=10M)", mt_yyscxz_cell);
        SHEET_TITLE_MAP.put("LTE下载低速率路段(<=2M)", mt_yyscxz_cell);
        SHEET_TITLE_MAP.put("LTE上传低速率路段(小于等于512K)", mt_yyscxz_cell);
        SHEET_TITLE_MAP.put("LTE下载低速率路段(小于等于10M)", mt_yyscxz_cell);
        SHEET_TITLE_MAP.put("LTE下载低速率路段(小于等于2M)", mt_yyscxz_cell);
        SHEET_TITLE_MAP.put("CSFB事件列表", mt_csfbcgl_cell);
        SHEET_TITLE_MAP.put("GSM连续质差路段", mt_gsmlxcld_cell);
        SHEET_TITLE_MAP.put("VOLTE_RTP丢包事件", mt_voltertodbsj_week);
        SHEET_TITLE_MAP.put("VOLTE掉话事件", mt_voltedhsj_week);
        SHEET_TITLE_MAP.put("VOLTE_持续弱MOS事件", mt_voltecxrmossj_week);
        SHEET_TITLE_MAP.put("VOLTE未接通事件", mt_voltenoconnection_week);
        SHEET_TITLE_MAP.put("LTE连续质差路段(SINR≤-3dB)", mt_lxzc_cell);
        SHEET_TITLE_MAP.put("LTE弱覆盖路段(RSRP≤-100)", mt_rfg_cell);
        SHEET_TITLE_MAP.put("LTE弱覆盖路段(RSRP≤-110)", mt_rfg_cell);
        SHEET_TITLE_MAP.put("LTE连续质差路段(SINR≤0dB)", mt_lxzc_cell);
        SHEET_TITLE_MAP.put("LTE连续质差路段(SINR小于等于-3dB)", mt_lxzc_cell);
        SHEET_TITLE_MAP.put("LTE弱覆盖路段(RSRP小于等于-100)", mt_rfg_cell);
        SHEET_TITLE_MAP.put("LTE弱覆盖路段(RSRP小于等于-110)", mt_rfg_cell);
        SHEET_TITLE_MAP.put("LTE连续质差路段(SINR小于等于0dB)", mt_lxzc_cell);
        SHEET_TITLE_MAP.put("eSVRCC切换差小区", mt_esvrccqhcxq_week);
        SHEET_TITLE_MAP.put("VoLTE E-RAB掉线高小区(视频)", mt_volteerabdxgxqsp_week);
        SHEET_TITLE_MAP.put("VoLTE E-RAB掉线高小区(语音)", mt_volteerabdxgxqyy_week);
        SHEET_TITLE_MAP.put("VOLTE_eSRVCC切换失败事件", mt_volteesrvccqhsbsj_week);
        SHEET_TITLE_MAP.put("VoLTE无线接通差小区(视频)", mt_voltewxjtcxqsp_week);
        SHEET_TITLE_MAP.put("VoLTE无线接通差小区(语音)", mt_voltewxjtcxqyy_week);
        SHEET_TITLE_MAP.put("VoLTE下行高时延小区", mt_voltexxgsyxq_week);
        SHEET_TITLE_MAP.put("LTE MR弱覆盖小区", mt_ltemrrfgxq_week);
        SHEET_TITLE_MAP.put("全网劣于竞争对手小区", mt_qwlyjzdsxq_week);
        SHEET_TITLE_MAP.put("GSM无线掉话率（一周出现大于3次低于目标值）小区", mt_gsmwxdhl_week);
        SHEET_TITLE_MAP.put("GSM无线接通率（一周出现大于3次低于目标值）小区", mt_gsmwxjtl_week);
        SHEET_TITLE_MAP.put("GSM质差话务比例", mt_gsmzchwbl_week);
        SHEET_TITLE_MAP.put("LTE掉线高小区", mt_ltewxdxl_week);
        SHEET_TITLE_MAP.put("LTE接通低小区", mt_ltewxjtl_week);
        SHEET_TITLE_MAP.put("LTE切换差小区", mt_lteqhcxq_week);
        SHEET_TITLE_MAP.put("高流量问题严重小区", mt_4ggllwtyzxq_week);
        SHEET_TITLE_MAP.put("LTE零流量小区", mt_ltelllxq_cell);
        SHEET_TITLE_MAP.put("倒流小区明细", mt_dlxqmx_week);
        SHEET_TITLE_MAP.put("信令道路覆盖问题点", yt_xldlfg_grid);
        SHEET_TITLE_MAP.put("信令道路干扰问题点", yt_xldlgr_grid);
        SHEET_TITLE_MAP.put("VOLTE用户切换差小区", mt_volteyhqhcxq_cell);
        SHEET_TITLE_MAP.put("QCI2承载切换差小区", mt_qci2czqhcxq_cell);
        SHEET_TITLE_MAP.put("VOLTE_IMS注册失败事件", mt_volteimszcsbsj_cell);
        SHEET_TITLE_MAP.put("VOLTE_eSRVCC切换时延-用户面(ms)", mt_volteesrvccqhsyyhm_cell);
        SHEET_TITLE_MAP.put("VOLTE呼叫建立时延", mt_voltehjjlsy_cell);
        SHEET_TITLE_MAP.put("投诉4G弱覆盖小区整治", mt_ts4grfgxqzz_cell);
        SHEET_TITLE_MAP.put("投诉补4G覆盖", mt_tsb4gfg_cell);
        
        SHEET_TITLE_MAP.put("VoLTE上行高丢包小区", mt_voltesxgdbxq_cell);
        SHEET_TITLE_MAP.put("VoLTE下行高丢包小区", mt_voltexxgdbxq_cell);

        SHEET_TITLE_MAP.put("用户投诉", yy_complainproblem);
    }

    public static final Map<String, String> SHEET_TABLE_MAP = new HashMap<String, String>();
    static {
        SHEET_TABLE_MAP.put("2G高流量宏站无LTE覆盖小区", "mt_2ggllhznolte_cell_");
        SHEET_TABLE_MAP.put("2G高流量室分无LTE覆盖小区", "mt_2ggllsfnolte_cell_");
        SHEET_TABLE_MAP.put("GSM高流量小区（一周）", "mt_gsmgllxq_cell_");
        SHEET_TABLE_MAP.put("高干扰小区", "mt_ggrxq_cell_");
        SHEET_TABLE_MAP.put("LTE超高站小区", "mt_ltecgxq_cell_");
        SHEET_TABLE_MAP.put("LTE超近站小区", "mt_ltecjxq_cell_");
        SHEET_TABLE_MAP.put("LTE超远站小区", "mt_ltecyxq_cell_");
        SHEET_TABLE_MAP.put("同频高重叠覆盖小区", "mt_tpgcdfgxq_cell_");
        SHEET_TABLE_MAP.put("LTE高重叠覆盖路段概要信息", "mt_cdfggy_cell_");
        SHEET_TABLE_MAP.put("LTE高重叠覆盖路段详细信息", "mt_cdfgxx_cell_");
        SHEET_TABLE_MAP.put("高负荷待扩容小区（新算法）", "mt_gfhdhrxq_cell_");
        SHEET_TABLE_MAP.put("LTE上传低速率路段(<=512K)", "mt_yyscxz_cell_");
        SHEET_TABLE_MAP.put("LTE下载低速率路段(<=10M)", "mt_yyscxz_cell_");
        SHEET_TABLE_MAP.put("LTE下载低速率路段(<=2M)", "mt_yyscxz_cell_");
        SHEET_TABLE_MAP.put("CSFB事件列表", "mt_csfbcgl_cell_");
        SHEET_TABLE_MAP.put("GSM连续质差路段", "mt_gsmlxcld_cell_");
        SHEET_TABLE_MAP.put("VOLTE_RTP丢包事件", "mt_voltertodbsj_cell_");
        SHEET_TABLE_MAP.put("VOLTE掉话事件", "mt_voltedhsj_cell_");
        SHEET_TABLE_MAP.put("VOLTE_持续弱MOS事件", "mt_voltecxrmossj_cell_");
        SHEET_TABLE_MAP.put("VOLTE未接通事件", "mt_voltenoconnection_cell_");
        SHEET_TABLE_MAP.put("LTE连续质差路段(SINR≤-3dB)", "mt_lxzc_cell_");
        SHEET_TABLE_MAP.put("LTE弱覆盖路段(RSRP≤-100)", "mt_rfg_cell_");
        SHEET_TABLE_MAP.put("LTE弱覆盖路段(RSRP≤-110)", "mt_rfg_cell_");
        SHEET_TABLE_MAP.put("LTE连续质差路段(SINR≤0dB)", "mt_lxzc_cell_");
        SHEET_TABLE_MAP.put("eSVRCC切换差小区", "mt_esvrccqhcxq_cell_");
        SHEET_TABLE_MAP.put("VoLTE E-RAB掉线高小区(视频)", "mt_volteerabdxgxqsp_cell_");
        SHEET_TABLE_MAP.put("VoLTE E-RAB掉线高小区(语音)", "mt_volteerabdxgxqyy_cell_");
        SHEET_TABLE_MAP.put("VOLTE_eSRVCC切换失败事件", "mt_volteesrvccqhsbsj_cell_");
        SHEET_TABLE_MAP.put("VoLTE无线接通差小区(视频)", "mt_voltewxjtcxqsp_cell_");
        SHEET_TABLE_MAP.put("VoLTE无线接通差小区(语音)", "mt_voltewxjtcxqyy_cell_");
        SHEET_TABLE_MAP.put("VoLTE下行高时延小区", "mt_voltexxgsyxq_cell_");
        SHEET_TABLE_MAP.put("LTE MR弱覆盖小区", "mt_ltemrrfgxq_cell_");
        SHEET_TABLE_MAP.put("全网劣于竞争对手小区", "mt_qwlyjzdsxq_cell_");
        SHEET_TABLE_MAP.put("GSM无线掉话率（一周出现大于3次低于目标值）小区", "mt_gsmwxdhl_cell_");
        SHEET_TABLE_MAP.put("GSM无线接通率（一周出现大于3次低于目标值）小区", "mt_gsmwxjtl_cell_");
        SHEET_TABLE_MAP.put("GSM质差话务比例", "mt_gsmzchwbl_cell_");
        SHEET_TABLE_MAP.put("LTE掉线高小区", "mt_ltewxdxl_cell_");
        SHEET_TABLE_MAP.put("LTE接通低小区", "mt_ltewxjtl_cell_");
        SHEET_TABLE_MAP.put("LTE切换差小区", "mt_lteqhcxq_cell_");
        SHEET_TABLE_MAP.put("高流量问题严重小区", "mt_4ggllwtyzxq_cell_");
        SHEET_TABLE_MAP.put("LTE零流量小区", "mt_ltelllxq_cell_");
        SHEET_TABLE_MAP.put("倒流小区明细", "mt_dlxqmx_cell_");
        SHEET_TABLE_MAP.put("信令道路覆盖问题点", "yt_xldlfg_grid_");
        SHEET_TABLE_MAP.put("信令道路干扰问题点", "yt_xldlgr_grid_");
        SHEET_TABLE_MAP.put("VOLTE用户切换差小区", "mt_volteyhqhcxq_cell_");
        SHEET_TABLE_MAP.put("QCI2承载切换差小区","mt_qci2czqhcxq_cell_");
        SHEET_TABLE_MAP.put("volte_ims注册失败事件", "mt_volteimszcsbsj_cell_");
        SHEET_TABLE_MAP.put("VOLTE_eSRVCC切换时延-用户面(ms)", "mt_volteesrvccqhsyyhm_cell_");
        SHEET_TABLE_MAP.put("VOLTE呼叫建立时延", "mt_voltehjjlsy_cell_");
        SHEET_TABLE_MAP.put("用户投诉", "yy_complainproblem_");
    }

    public static final Map<String, String> TABLE_DATASOURCE_MAP = new HashMap<String, String>();
    static {
        TABLE_DATASOURCE_MAP.put("mt_2ggllhznolte_cell_", "PM");
        TABLE_DATASOURCE_MAP.put("mt_2ggllsfnolte_cell_", "PM");
        TABLE_DATASOURCE_MAP.put("mt_gsmgllxq_cell_", "PM");
        TABLE_DATASOURCE_MAP.put("mt_ggrxq_cell_", "PM");
        TABLE_DATASOURCE_MAP.put("mt_ltecgxq_cell_", "CM");
        TABLE_DATASOURCE_MAP.put("mt_ltecjxq_cell_", "CM");
        TABLE_DATASOURCE_MAP.put("mt_ltecyxq_cell_", "CM");
        TABLE_DATASOURCE_MAP.put("mt_tpgcdfgxq_cell_", "PM");
        TABLE_DATASOURCE_MAP.put("mt_cdfggy_cell_", "ATU");
        TABLE_DATASOURCE_MAP.put("mt_cdfgxx_cell_", "ATU");
        TABLE_DATASOURCE_MAP.put("mt_gfhdhrxq_cell_", "PM");
        TABLE_DATASOURCE_MAP.put("mt_yyscxz_cell_", "ATU");
//        TABLE_DATASOURCE_MAP.put("mt_yyscxz_cell_", "ATU");
//        TABLE_DATASOURCE_MAP.put("mt_yyscxz_cell_", "ATU");
        TABLE_DATASOURCE_MAP.put("mt_csfbcgl_cell_", "ATU");
        TABLE_DATASOURCE_MAP.put("mt_gsmlxcld_cell_", "ATU");
        TABLE_DATASOURCE_MAP.put("mt_voltertodbsj_cell_", "ATU");
        TABLE_DATASOURCE_MAP.put("mt_voltedhsj_cell_", "ATU");
        TABLE_DATASOURCE_MAP.put("mt_voltecxrmossj_cell_", "ATU");
        TABLE_DATASOURCE_MAP.put("mt_voltenoconnection_cell_", "ATU");
        TABLE_DATASOURCE_MAP.put("mt_lxzc_cell_", "ATU");
        TABLE_DATASOURCE_MAP.put("mt_rfg_cell_", "ATU");
//        TABLE_DATASOURCE_MAP.put("mt_rfg_cell_", "ATU");
//        TABLE_DATASOURCE_MAP.put("mt_lxzc_cell_", "ATU");
        TABLE_DATASOURCE_MAP.put("mt_esvrccqhcxq_cell_", "PM");
        TABLE_DATASOURCE_MAP.put("mt_volteerabdxgxqsp_cell_", "PM");
        TABLE_DATASOURCE_MAP.put("mt_volteerabdxgxqyy_cell_", "PM");
        TABLE_DATASOURCE_MAP.put("mt_volteesrvccqhsbsj_cell_", "ATU");
        TABLE_DATASOURCE_MAP.put("mt_voltewxjtcxqsp_cell_", "PM");
        TABLE_DATASOURCE_MAP.put("mt_voltewxjtcxqyy_cell_", "PM");
        TABLE_DATASOURCE_MAP.put("mt_voltexxgsyxq_cell_", "PM");
        TABLE_DATASOURCE_MAP.put("mt_ltemrrfgxq_cell_", "MR");
        TABLE_DATASOURCE_MAP.put("mt_qwlyjzdsxq_cell_", "MR");
        TABLE_DATASOURCE_MAP.put("mt_gsmwxdhl_cell_", "PM");
        TABLE_DATASOURCE_MAP.put("mt_gsmwxjtl_cell_", "PM");
        TABLE_DATASOURCE_MAP.put("mt_gsmzchwbl_cell_", "PM");
        TABLE_DATASOURCE_MAP.put("mt_ltewxdxl_cell_", "PM");
        TABLE_DATASOURCE_MAP.put("mt_ltewxjtl_cell_", "PM");
        TABLE_DATASOURCE_MAP.put("mt_lteqhcxq_cell_", "PM");
        TABLE_DATASOURCE_MAP.put("mt_4ggllwtyzxq_cell_", "PM");
        TABLE_DATASOURCE_MAP.put("mt_ltelllxq_cell_", "PM");
        TABLE_DATASOURCE_MAP.put("yt_xldlfg_grid_", "XL");
        TABLE_DATASOURCE_MAP.put("yt_xldlgr_grid_", "XL");
        TABLE_DATASOURCE_MAP.put("mt_volteyhqhcxq_cell_", "PM");
        TABLE_DATASOURCE_MAP.put("mt_qci2czqhcxq_cell_", "PM");
        TABLE_DATASOURCE_MAP.put("mt_volteimszcsbsj_cell_", "ATU");
        TABLE_DATASOURCE_MAP.put("mt_volteesrvccqhsyyhm_cell_", "ATU");
        TABLE_DATASOURCE_MAP.put("mt_voltehjjlsy_cell_", "ATU");
        TABLE_DATASOURCE_MAP.put("yy_complainproblem_", "投诉系统");
    }

    public static final String[] tableList = new String[] {"mt_2ggllhznolte_cell_", "mt_2ggllsfnolte_cell_", "mt_gsmgllxq_cell_",
            "mt_ggrxq_cell_", "mt_ltecgxq_cell_", "mt_ltecjxq_cell_", "mt_ltecyxq_cell_","mt_tpgcdfgxq_cell_", "mt_cdfggy_cell_", "mt_cdfgxx_cell_",
            "mt_gfhdhrxq_cell_", "mt_yyscxz_cell_", "mt_csfbcgl_cell_", "mt_gsmlxcld_cell_",
            "mt_voltertodbsj_cell_", "mt_voltedhsj_cell_", "mt_voltecxrmossj_cell_", "mt_voltenoconnection_cell_", "mt_lxzc_cell_",
            "mt_rfg_cell_", "mt_esvrccqhcxq_cell_", "mt_volteerabdxgxqsp_cell_", "mt_volteerabdxgxqyy_cell_", "mt_volteesrvccqhsbsj_cell_",
            "mt_voltewxjtcxqsp_cell_", "mt_voltewxjtcxqyy_cell_", "mt_voltexxgsyxq_cell_", "mt_ltemrrfgxq_cell_","mt_qwlyjzdsxq_cell_", "mt_gsmwxdhl_cell_",
            "mt_gsmwxjtl_cell_", "mt_gsmzchwbl_cell_", "mt_ltewxdxl_cell_", "mt_ltewxjtl_cell_","mt_lteqhcxq_cell_","mt_4ggllwtyzxq_cell_","mt_ltelllxq_cell_", "mt_dlxqmx_cell_","yt_xldlfg_grid_","yt_xldlgr_grid_",
            "mt_volteyhqhcxq_cell_","mt_volteimszcsbsj_cell_","mt_volteesrvccqhsyyhm_cell_","mt_voltehjjlsy_cell_","yy_complainproblem_"};  //"mt_qci2czqhcxq_cell_",表不存在

    public static final String[] tableList_1 = new String[] {"mt_2ggllhznolte_cell_", "mt_2ggllsfnolte_cell_", "mt_gsmgllxq_cell_",
            "mt_ggrxq_cell_", "mt_ltecgxq_cell_", "mt_ltecjxq_cell_", "mt_ltecyxq_cell_","mt_tpgcdfgxq_cell_", "mt_cdfggy_cell_", "mt_cdfgxx_cell_",
            "mt_gfhdhrxq_cell_", "mt_yyscxz_cell_", "mt_csfbcgl_cell_", "mt_gsmlxcld_cell_"};

    public static final String[] tableList_2 = new String[] {"mt_voltertodbsj_cell_", "mt_voltedhsj_cell_", "mt_voltecxrmossj_cell_", "mt_voltenoconnection_cell_", "mt_lxzc_cell_",
            "mt_rfg_cell_", "mt_esvrccqhcxq_cell_", "mt_volteerabdxgxqsp_cell_", "mt_volteerabdxgxqyy_cell_", "mt_volteesrvccqhsbsj_cell_",
            "mt_voltewxjtcxqsp_cell_", "mt_voltewxjtcxqyy_cell_", "mt_voltexxgsyxq_cell_", "mt_ltemrrfgxq_cell_","mt_qwlyjzdsxq_cell_", "mt_gsmwxdhl_cell_"};

    public static final String[] tableList_3 = new String[] {"mt_gsmwxjtl_cell_", "mt_gsmzchwbl_cell_", "mt_ltewxdxl_cell_", "mt_ltewxjtl_cell_","mt_lteqhcxq_cell_","mt_4ggllwtyzxq_cell_","mt_ltelllxq_cell_", "mt_dlxqmx_cell_","yt_xldlfg_grid_","yt_xldlgr_grid_",
            "mt_volteyhqhcxq_cell_","mt_volteimszcsbsj_cell_","mt_volteesrvccqhsyyhm_cell_","mt_voltehjjlsy_cell_"};



    public static final String[] tableListTT = new String[] {"yt_xldlfg_grid_","yt_xldlgr_grid_"};

    //    vcFileName,vcRoadName,vcCellName,datetime都有
    public static final String[] tableList1 = new String[] {"mt_yyscxz_cell_", "mt_gsmlxcld_cell_", "mt_lxzc_cell_", "mt_rfg_cell_"};

//    vcFileName,vcRoadName,vcCellName,datetime都没有
    public static final String[] tableList2 = new String[] {"mt_cdfgxx_cell_", "mt_esvrccqhcxq_cell_", "mt_volteerabdxgxqyy_cell_",
            "mt_voltewxjtcxqyy_cell_", "mt_voltexxgsyxq_cell_", "mt_gsmwxdhl_cell_", "mt_gsmwxjtl_cell_", "mt_gsmzchwbl_cell_",
            "mt_ltewxdxl_cell_", "mt_ltewxjtl_cell_","mt_lteqhcxq_cell_","mt_4ggllwtyzxq_cell_","mt_ltelllxq_cell_","mt_volteyhqhcxq_cell_"};

//    只有datetime
    public static final String[] tableList3 = new String[] {"mt_2ggllhznolte_cell_", "mt_2ggllsfnolte_cell_", "mt_gsmgllxq_cell_", "mt_ggrxq_cell_",
            "mt_ltecgxq_cell_", "mt_ltecjxq_cell_", "mt_ltecyxq_cell_","mt_tpgcdfgxq_cell_", "mt_gfhdhrxq_cell_", "mt_ltemrrfgxq_cell_","mt_qwlyjzdsxq_cell_", "mt_dlxqmx_cell_","yt_xldlfg_grid_",
            "yt_xldlgr_grid_","mt_voltehjjlsy_cell_"};

//    有vcFileName,datetime
    public static final String[] tableList4 = new String[] {"mt_voltertodbsj_cell_", "mt_voltedhsj_cell_", "mt_voltecxrmossj_cell_",
            "mt_voltenoconnection_cell_", "mt_volteesrvccqhsbsj_cell_","mt_volteimszcsbsj_cell_","mt_volteesrvccqhsyyhm_cell_"};

//    有vcFileName,vcCellName,datetime
    public static final String[] tableList5 = new String[] {"mt_csfbcgl_cell_"};

//    只有vcRoadName
    public static final String[] tableList6 = new String[] {"mt_cdfggy_cell_"};
//	mt_ggrxq_cell_, mt_cdfggy_cell_, mt_volteerabdxgxqsp_cell_, mt_voltewxjtcxqsp_cell_没有原始数据

    //  有vcCellName
    // mt_qci2czqhcxq_cell_   表不存在
    //  有vcFileName, datetime
    //    有vcFileName  datetime
    //    datetime




  }
