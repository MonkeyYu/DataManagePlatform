package com.etone.universe.dmp.problem;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 16-6-29
 * Time: 下午9:36
 * To change this template use File | Settings | File Templates.
 */
public class Common {
    private static final Logger logger = LoggerFactory.getLogger(Common.class);
    /**
    * 定义分割常量 （#在集合中的含义是每个元素的分割，|主要用于map类型的集合用于key与value中的分割）
    */
    private static final String SEP1 = "#";
    private static final String SEP2 = "|";

    public static int download_times = 0;

    public static String buildXmlFile(Document doc) {
        String xmlString = "";
        String path = getRealPath() + File.separator + "WEB-INF" + File.separator + "uploadfile" + File.separator;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }

        try {
            Format format = Format.getPrettyFormat();
            format.setEncoding("UTF-8");
            XMLOutputter docWriter = new XMLOutputter();
            docWriter.setFormat(format);
            docWriter.output(doc, new FileOutputStream(path + getDateString3(new Date()) + ".xml"));
            xmlString = docWriter.outputString(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return xmlString;
    }

    public static String getRealPath() {
        URL url = Thread.currentThread().getContextClassLoader().getResource("/");
        if (url == null) {
            url = Common.class.getResource("/");
        }
        File file = null;
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            logger.error("", e);
        }
        while (file != null) {
            if (file.getName().equals("WEB-INF")) {
                file = file.getParentFile();
                break;
            }
            file = file.getParentFile();
        }
        return file.getAbsolutePath();
    }

    public static String getDateString2(Date date) {
        return getDateString(date, "yyyyMM");
    }

    public static String getDateString3(Date date) {
        return getDateString(date, "yyyyMMdd");
    }

    public static String getDateString(Date date, String sFormat) {
        SimpleDateFormat format = new SimpleDateFormat(sFormat);
        if (date != null) {
            return format.format(date);
        }
        return "";
    }

    public static Date getDate(String s) {
        try {
            if (!judgeString(s))
                return null;
            if (s.length() < 11) {
                s = s + " 01:01:01";
            }
            SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return simpledateformat.parse(s);
        } catch (Exception _ex) {
            _ex.printStackTrace();
            return null;
        }
    }

    public static boolean judgeString(String str) {
        boolean flag = false;
        if (str != null && !(str.trim()).equals("")
                && !str.equalsIgnoreCase("null")) {
            flag = true;
        }
        return flag;
    }

    public static String addInteger(String dateStr, int amount) {
        Date myDate = null;
        String myDateStr = "";
        try {
            SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = simpledateformat.parse(dateStr);

            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.DATE, amount);
                myDate = calendar.getTime();
                myDateStr = simpledateformat.format(myDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return myDateStr;
    }

    public static String convertCity(String cityCode) {
        String cityName = "";
        if ("gd".equalsIgnoreCase(cityCode)) {
            cityName = "省公司";
        } else if ("cz".equalsIgnoreCase(cityCode)) {
            cityName = "潮州";
        } else if ("dg".equalsIgnoreCase(cityCode)) {
            cityName = "东莞";
        } else if ("fs".equalsIgnoreCase(cityCode)) {
            cityName = "佛山";
        } else if ("gz".equalsIgnoreCase(cityCode)) {
            cityName = "广州";
        } else if ("zh".equalsIgnoreCase(cityCode)) {
            cityName = "珠海";
        } else if ("hz".equalsIgnoreCase(cityCode)) {
            cityName = "惠州";
        } else if ("jm".equalsIgnoreCase(cityCode)) {
            cityName = "江门";
        } else if ("jy".equalsIgnoreCase(cityCode)) {
            cityName = "揭阳";
        } else if ("mm".equalsIgnoreCase(cityCode)) {
            cityName = "茂名";
        } else if ("mz".equalsIgnoreCase(cityCode)) {
            cityName = "梅州";
        } else if ("qy".equalsIgnoreCase(cityCode)) {
            cityName = "清远";
        } else if ("st".equalsIgnoreCase(cityCode)) {
            cityName = "汕头";
        } else if ("sw".equalsIgnoreCase(cityCode)) {
            cityName = "汕尾";
        } else if ("sg".equalsIgnoreCase(cityCode)) {
            cityName = "韶关";
        } else if ("sz".equalsIgnoreCase(cityCode)) {
            cityName = "深圳";
        } else if ("yj".equalsIgnoreCase(cityCode)) {
            cityName = "阳江";
        } else if ("yf".equalsIgnoreCase(cityCode)) {
            cityName = "云浮";
        } else if ("zj".equalsIgnoreCase(cityCode)) {
            cityName = "湛江";
        } else if ("zq".equalsIgnoreCase(cityCode)) {
            cityName = "肇庆";
        } else if ("zs".equalsIgnoreCase(cityCode)) {
            cityName = "中山";
        } else if ("hy".equalsIgnoreCase(cityCode)) {
            cityName = "河源";
        }
        return cityName;
    }

    public static Document convertStringToDocument(String xmlString) {
        StringReader sr = null;
        InputSource is = null;
        Document doc = null;
        String[] resultInfo = new String[3];
        try {
            sr = new StringReader(xmlString);
            is = new InputSource(sr);
            doc = (new SAXBuilder()).build(is);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return doc;
    }

    public static String[] convertStringToXml(String xmlString) {
        StringReader sr = null;
        InputSource is = null;
        Document doc = null;
        String[] resultInfo = new String[3];
        try {
            sr = new StringReader(xmlString);
            is = new InputSource(sr);
            doc = (new SAXBuilder()).build(is);
            Element root = doc.getRootElement();
            List jiedian = root.getChildren();
            Element et = null;
            for(int i=0;i < jiedian.size(); i++) {
                et = (Element) jiedian.get(i);//循环依次得到子元素
                resultInfo[i] = et.getValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return resultInfo;
    }

    public static void copyFile(InputStream inputStream, String descPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            int pos = descPath.lastIndexOf(File.separator);
            if (pos > -1) {
                File folder = new File(descPath.substring(0, pos));
                if (!folder.exists() || !folder.isDirectory()) {
                    folder.mkdirs();
                }
            }
            File file = new File(descPath);
            if (file.exists()) {
                file.delete();
            }

            in = new BufferedInputStream(inputStream, 1024);
            out = new BufferedOutputStream(new FileOutputStream(file), 1024);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != in) {
                    in.close();
                }
                if (null != out) {
                    out.close();
                }
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }

    public static void mkdir(String path) {
        try {
            int pos = path.lastIndexOf(File.separator);
            if (pos > -1) {
                File folder = new File(path.substring(0, pos));
                if (!folder.exists() || !folder.isDirectory()) {
                    folder.mkdirs();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 下划线分隔的字符串，转换为驼峰式字符串
     * @param param
     * @return
     */
    public static String underlineToCamel(String param){
        Pattern p = Pattern.compile("_[0-9|a-z]");
        Matcher m = p.matcher(param);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String firstChar =  m.group().substring(1, 2);
            m.appendReplacement(sb, firstChar.toUpperCase());
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static String convertInteger(String name) {
        boolean flag = true;
        if (name.contains(".")) {
            String[] numbers = name.split("\\.");
            char[] ch = numbers[1].toCharArray();
            for (int i = 0; i < ch.length; i++) {
                if (!"0".equals(String.valueOf(ch[i]))) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                name = name.substring(0, name.indexOf("."));
            }
        }
        return name;
    }

    /**
     * @param list
     *            若list保存的为对象，则抛出异常
     * @param flag
     *            标识符
     * @return 若flag 为"1", 返回格式为 '1', '2', '3'; 否则返回格式 1, 2, 3
     */
    public static String ListToString(List<?> list, String flag) {
        try {
            String str = list.toString();
            if (judgeString(flag) && flag.equals("1")) {
                str = str.replaceAll("(\\[|\\])", "'");
                return str.replace(", ", "', '");
            } else {
                return str.substring(1, str.length() - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] ars) {
        int size = 1;
        int sizeCount = size % 1000 == 0 ? size / 1000 : size / 1000 + 1;
        for (int i = 0; i < sizeCount; i++) {
            if ((i + 1) * 1000 > size) {
                if (i * 1000 == 0) {
                    if (size == 1) {
                        System.out.println(size);
                    } else {
                        System.out.println(i * 1000 + "==" + (size - 1));
                    }
//                    criteria.put("saveList", saveList.subList(i * 1000, size - 1));
                } else {
                    System.out.println((i * 1000 - 1) + "==" + (size - 1));
//                    criteria.put("saveList", saveList.subList(i * 1000 - 1, size - 1));
                }
            } else {
                if (i * 1000 == 0) {
                    System.out.println((i * 1000) + "==" + ((i + 1) * 1000 - 1));
//                    criteria.put("saveList", saveList.subList(i * 1000, (i + 1) * 1000 - 1));
                } else {
                    System.out.println((i * 1000 - 1) + "==" + ((i + 1) * 1000 - 1));
//                    criteria.put("saveList", saveList.subList(i * 1000 - 1, (i + 1) * 1000 - 1));
                }
            }
//            lteGenProposalMapper.saveProposal(criteria);
        }
    }

    public static String aa() {
        try {
            System.out.println("try:");
            return "1";
        } catch (Exception e) {
            System.out.println("catch:");
            e.printStackTrace();
        } finally {
            System.out.println("finally:");
        }
        return "2";
    }

    public static String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
            dest = dest.replaceAll("\"", "“");
            dest = dest.replaceAll("\\[", "【");
            dest = dest.replaceAll("]", "】");
            dest = dest.replaceAll("\\\\", "");
            dest = dest.replaceAll("/", "");
            dest = dest.replaceAll("\\(", "（");
            dest = dest.replaceAll("\\)", "）");
            dest = dest.replaceAll(",", "，");
        }
        return dest;
    }

    public static String retuenCity(String city){
        String citycode="";
        if(city.equals("肇庆")){
            citycode="QZ";
        }else if(city.equals("揭阳")){
            citycode="JY";
        }else if(city.equals("惠州")){
            citycode="HZ";
        }else if(city.equals("潮州")){
            citycode="CZ";
        }else if(city.equals("汕尾")){
            citycode="SW";
        }else if(city.equals("中山")){
            citycode="ZS";
        }else if(city.equals("广州")){
            citycode="GZ";
        }else if(city.equals("汕头")){
            citycode="ST";
        }else if(city.equals("佛山")){
            citycode="FS";
        }else if(city.equals("深圳")){
            citycode="SZ";
        }else if(city.equals("梅州")){
            citycode="MZ";
        }else if(city.equals("茂名")){
            citycode="MM";
        }else if(city.equals("东莞")){
            citycode="DG";
        }else if(city.equals("湛江")){
            citycode="ZJ";
        }else if(city.equals("阳江")){
            citycode="YJ";
        }else if(city.equals("江门")){
            citycode="JM";
        }else if(city.equals("云浮")){
            citycode="YF";
        }else if(city.equals("韶关")){
            citycode="SJ";
        }else if(city.equals("清远")){
            citycode="QY";
        }else if(city.equals("河源")){
            citycode="HY";
        }else{
            citycode="ZH";
        }
        return citycode;
    }

    /**
     *
     * 查询当前日期前(后)x天的日期
     *
     * @param date 当前日期
     * @param day 天数（如果day数为负数,说明是此日期前的天数）
     * @return yyyy-MM-dd
     */
    public static String beforNumDay(Date date, int day) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_YEAR, day);
        System.out.println( new SimpleDateFormat("yyyy-MM-dd").format(c.getTime()));
        return new SimpleDateFormat("yyyyMMdd").format(c.getTime());
    }


    public static int getDownload_times() {
        return download_times;
    }

    public static void setDownload_times(int download_times) {
        Common.download_times = download_times;
    }


    public static String CheckInfo(int number,String info) {
        if(number==1){
            if(!info.equals("")){
               if(!info.equals("故障")&&!info.equals("覆盖")&&!info.equals("结构")&&!info.equals("干扰")&&!info.equals("容量")&&!info.equals("参数")&&!info.equals("其他")&&!info.equals("其它")&&!info.equals("其他（核心侧、传输等）")&&!info.equals("其它（核心侧、传输等）")){
                   info="";
               }
            }
            }else if(number==2){
            if(!info.equals("")){
                if(!info.equals("故障")&&!info.equals("覆盖")&&!info.equals("结构")&&!info.equals("干扰")&&!info.equals("容量")&&!info.equals("参数")&&!info.equals("其他")&&!info.equals("其它")&&!info.equals("其他（核心侧、传输等）")&&!info.equals("其它（核心侧、传输等）")){
                    info="";
                }
            }
            }else if(number==3){
            if(!info.equals("")){
                if(!info.equals("新增规划")&&!info.equals("工程建设")&&!info.equals("工程整改")&&!info.equals("天面整改")&&!info.equals("天线调整")&&!info.equals("参数优化")&&!info.equals("维护")&&!info.equals("非无线网络原因")){
                    info="";
                }
            }
            }else {
            if(!info.equals("")){
                if(!info.equals("新增规划")&&!info.equals("工程建设")&&!info.equals("工程整改")&&!info.equals("天面整改")&&!info.equals("天线调整")&&!info.equals("参数优化")&&!info.equals("维护")&&!info.equals("其它")&&!info.equals("市公司现场排查")){
                    info="";
                }
            }
        }
        return info;
    }
}
