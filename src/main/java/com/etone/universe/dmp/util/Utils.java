package com.etone.universe.dmp.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

public class Utils {
    public static String[] split(String line, char split) {
        String[] sArray = null;

        ArrayList<Integer> idxs = new ArrayList<Integer>();

        int index = 0;

        for (char c : line.toCharArray()) {
            if (c == split) {
                idxs.add(index);
            }

            index++;
        }

        if (idxs.size() > 0) {
            sArray = new String[idxs.size() + 1];
            sArray[0] = line.substring(0, idxs.get(0));

            for (int i = 0; i < idxs.size() - 1; i++) {
                sArray[i + 1] = line.substring(idxs.get(i) + 1, idxs.get(i + 1));
            }

            sArray[idxs.size()] = line.substring(idxs.get(idxs.size() - 1) + 1);
        }

        return sArray;
    }

    public static Calendar getCalendar(String s, String format) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(format);

        try {
            Date date = sdf.parse(s);
            c.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        return c;
    }

    /**
     * 转换为GB2312编码
     *
     * @param str 要转换的字符串
     * @return 转换后的字符串, 如果转换失败则返回原字符串
     */
    public static String toGB2312(String str) {
        try {
            byte[] bys = str.getBytes("ISO8859_1");
            return new String(bys, "GB2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return str;
        }
    }

    /**
     * 转换为GBK编码
     *
     * @param str 要转换的字符串
     * @return 转换后的字符串, 如果转换失败则返回原字符串
     */
    public static String toGBK(String str) {
        try {
            byte[] bys = str.getBytes("ISO8859_1");
            return new String(bys, "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return str;
        }
    }

    /**
     * 转换为UTF-8编码
     *
     * @param str 要转换的字符串
     * @return 转换后的字符串, 如果转换失败则返回原字符串
     */
    public static String toUTF8(String str) {
        try {
            byte[] bys = str.getBytes("ISO8859_1");
            return new String(bys, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return str;
        }
    }

    /**
     * 转换为iso-8859-1编码. 一般用于把硬编码的中文字符串内容写入Sybase数据库中或得到在JAVA中的原始编码格式.
     *
     * @param str 要转换的字符串
     * @return 转换后的字符串, 如果转换失败则返回原字符串
     */
    public static String toISO_8859_1(String str) {
        try {
            byte[] bys = str.getBytes("GBK");
            return new String(bys, "ISO8859_1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return str;
        }
    }

    /**
     * 转换为GBK编码
     *
     * @param str 要转换的字符串
     * @return 转换后的字符串, 如果转换失败则返回原字符串
     */
    public static String convert(String str) {
        return toGBK(str);
    }

    /**
     * 转换为iso-8859-1编码. 一般用于把硬编码的中文字符串内容写入Sybase数据库中或得到在JAVA中的原始编码格式.
     *
     * @param str 要转换的字符串
     * @return 转换后的字符串, 如果转换失败则返回原字符串
     */
    public static String disconvert(String str) {
        return toISO_8859_1(str);
    }

    /**
     * 转换指定精度的浮点数
     *
     * @param d         原始浮点数
     * @param precision 精度(指小数点后的位数).如果指定为-1,则取整.
     * @return 转换后的浮点数字符串
     */
    public static String getDoubleString(double d, int precision) {
        String value = String.valueOf(d);
        return getDoubleString(value, precision);
    }

    /**
     * 转换指定精度的浮点数
     *
     * @param str       浮点数字符串
     * @param precision 精度(指小数点后的位数).如果指定为-1,则取整.
     * @return 转换后的浮点数字符串
     */
    public static String getDoubleString(String str, int precision) {
        if (str.substring(str.indexOf("."), str.length()).length() > precision + 1)
            str = str.substring(0, str.indexOf(".") + precision + 1);
        return str;
    }

    /**
     * 返回date所在的一周内的第一天(星期一)的日期
     *
     * @param date 指定日期的字符串,格式为: yyyy-MM-dd
     * @return 返回第一天的日期字符串, 格式为: yyyy-MM-dd
     */
    public static String getDateOfWeekBegin(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = GregorianCalendar.getInstance();
            cal.setTime(sdf.parse(date));

            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            // 因为Calendar默认星期日是一个星期的第一天,对于目前本函数应用需进行修正处理
            if (--dayOfWeek == 0)
                dayOfWeek = 7;

            cal.add(Calendar.DATE, -(dayOfWeek - 1)); // 减去天数

            String result = cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DATE);
            return result;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 返回date所在的一周内的最后一天(星期日)的日期
     *
     * @param date 指定日期的字符串,格式为: yyyy-MM-dd
     * @return 返回最后一天的日期字符串, 格式为: yyyy-MM-dd
     */
    public static String getDateOfWeekEnd(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = GregorianCalendar.getInstance();
            cal.setTime(sdf.parse(date));

            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            // 因为Calendar默认星期日是一个星期的第一天,对于目前本函数应用需进行修正处理
            if (--dayOfWeek == 0)
                dayOfWeek = 7;

            cal.add(Calendar.DATE, (7 - dayOfWeek)); // 增加天数

            String result = cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DATE);
            return result;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 转换Image数据为byte数组
     *
     * @param image  Image对象
     * @param format image格式字符串.如"jpeg","png"
     * @return byte数组
     */
    public static byte[] imageToBytes(Image image, String format) {
        BufferedImage bImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics bg = bImage.getGraphics();
        bg.drawImage(image, 0, 0, null);
        bg.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(bImage, format, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    /**
     * 转换byte数组为Image
     *
     * @param bytes Image的bytes数据数组
     * @return Image
     */
    public static Image bytesToImage(byte[] bytes) {
        Image image = Toolkit.getDefaultToolkit().createImage(bytes);

        try {
            MediaTracker mt = new MediaTracker(new Label());
            mt.addImage(image, 0);
            mt.waitForAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return image;
    }

    /**
     * 判断是否为闰年
     *
     * @param year 年份
     * @return 是否为闰年.true:是,false:不是.
     */
    public static boolean isLeapYear(int year) {
        return (year % 4 == 0) && (year % 100 != 0) || (year % 400 == 0);
    }

    /**
     * 返回指定对象在对象数组中的索引号
     *
     * @param objects 对象数组
     * @param object  对象
     * @return 对象在对象数组中的索引号, 没有匹配时返回-1
     */
    public static int indexOfArray(Object[] objects, Object object) {
        for (int i = 0; i < objects.length; i++) {
            if (objects[i].equals(object))
                return i;
        }
        return -1;
    }

    /**
     * 判断是否为整数
     *
     * @param value 要判断的内容
     * @return true:是,false:不是
     */
    public static boolean isInteger(String value) {
        return Pattern.matches("[0-9]+", value);
    }

    /**
     * 判断是否为浮点数
     *
     * @param value 要判断的内容
     * @return true:是,false:不是
     */
    public static boolean isFloat(String value) {
        return Pattern.matches("[0-9]+.[0-9]+", value);
    }

    /**
     * 判断是否为十六进制数
     *
     * @param value 要判断的内容
     * @return true:是,false:不是
     */
    public static boolean isHex(String value) {
        return Pattern.matches("0?[x||X]?[a-fA-F[0-9]]+", value);
    }

    /**
     * 判断是否为八进制数
     *
     * @param value 要判断的内容
     * @return true:是,false:不是
     */
    public static boolean isOct(String value) {
        return Pattern.matches("0?[0-7]+", value);
    }

    /**
     * 判断是否为合法的Email地址 (未验证)
     *
     * @param value 要判断的内容
     * @return true:是,false:不是
     */
    public static boolean isEmailAddress(String value) {
        return Pattern.matches("[a-zA-Z[0-9]_]+@[a-zA-Z[0-9]-]+.[a-zA-Z[0-9]_]+", value);
    }

    /**
     * 输出列表内容
     *
     * @param list 列表
     */
    public static void printValue(String prefix, ArrayList<Object> list, String suffix) {
        for (int i = 0; i < list.size(); i++) {
            System.out.print(prefix);
            System.out.print(list.get(i));
            System.out.println(suffix);
        }
    }

    /**
     * 输出对象数组内容
     *
     * @param objects 数组
     */
    public static void printValue(String prefix, Object[] objects, String suffix) {
        for (int i = 0; i < objects.length; i++) {
            System.out.print(prefix);
            System.out.print(objects[i].toString());
            System.out.println(suffix);
        }
    }

    /**
     * 输出字节数组内容
     *
     * @param prefix 输入每单位内容的前缀
     * @param bytes  要出的字节数组
     * @param suffix 输入每单位内容的后缀
     */
    public static void printValue(String prefix, byte[] bytes, String suffix) {
        for (int i = 0; i < bytes.length; i++) {
            System.out.print(prefix);
            System.out.print(Integer.toHexString(bytes[i] & 0xFF));
            System.out.print(suffix);
        }
        System.out.println();
    }

    /**
     * 输出日历时间
     *
     * @param cal 日历对象
     */
    public static void printValue(String prefix, Calendar cal, String suffix) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd E HH:mm:ss.S");
        System.out.println(prefix + sdf.format(cal.getTime()) + suffix);
    }

    /**
     * 将时间字符串转换为时间
     * @param dateStr
     * @param format
     * @return
     */
    public static Date str2Date(String dateStr, String format) {
        SimpleDateFormat org = new SimpleDateFormat(format);
        try {
            return org.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    /**
     * 字符串 左 对齐
     *
     * @param source 要处理的源字符串
     * @param len    限定对齐长度
     * @param fill   不足长度填充的字符
     * @return 对齐后的字符串
     */
    public static String alignLeft(String source, int len, byte fill) {
        if (source == null) {
            source = "";
        }

        if (source.length() > len)
            return source.substring(0, len);

        if (source.length() < len) {
            StringBuffer sb = new StringBuffer();
            sb.append(source);
            for (int i = source.length(); i < len; i++)
                sb.append((char) fill);
            return sb.toString();
        }

        return source;
    }

    /**
     * 字符串 右 对齐
     *
     * @param source 要处理的源字符串
     * @param len    限定对齐长度
     * @param fill   不足长度填充的字符
     * @return 对齐后的字符串
     */
    public static String alignRight(String source, int len, byte fill) {
        if (source.length() > len)
            return source.substring(0, len);

        if (source.length() < len) {
            String prefix = "";
            for (int i = 0; i < len - source.length(); i++)
                prefix += (char) fill;
            return prefix + source;
        }

        return source;
    }

    /**
     * 比较两个字节数组是否相同
     *
     * @param src  比较源
     * @param dest 比较目标
     * @return true:相同,false:不相同
     */
    public static boolean compareBytes(byte[] src, byte[] dest) {
        if (src.length != dest.length)
            return false;

        for (int i = 0; i < src.length; i++)
            if (src[i] != dest[i])
                return false;

        return true;
    }

    public static String getDateTimeFormat(Date d) {
        if (d == null)
            return "1900-00-00 00:00:00";

        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d);
    }

    public static byte[] getMD5(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            md.update(data.getBytes());
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isGBK(String src) {
        byte[] bytes = src.getBytes();

        byte head = bytes[0];
        byte tail = bytes[bytes.length - 1];
        int iHead = head & 0xff;
        int iTail = tail & 0xff;
        return ((iHead >= 0x81 && iHead <= 0xfe &&
                (iTail >= 0x40 && iTail <= 0x7e ||
                        iTail >= 0x80 && iTail <= 0xfe)) ? true : false);
    }

    /**
     * 扫描目录下的所有问题，不遍历子文件夹
     *
     * @param path
     * @param prefix
     * @param suffix
     * @return
     */
    public static File[] scan(String path, final String prefix, final String suffix) {
        File dir = new File(path);

        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.getName().toLowerCase().startsWith(prefix.toLowerCase()) && f.getName().toLowerCase().endsWith(suffix.toLowerCase())) {
                    return true;
                }
                return false;
            }
        });

        // 按文件名进行排序
        for (int i = 0; i < files.length; i++) {
            for (int j = i + 1; j < files.length; j++) {
                if (files[i].getName().compareTo(files[j].getName()) > 0) {
                    File temp = files[i];
                    files[i] = files[j];
                    files[j] = temp;
                }
            }
        }

        return files;
    }

    /**
     * 递归遍历目录下的所有文件，返回文件列表
     *
     * @param path
     * @param prefix
     * @param suffix
     * @param limt
     * @return
     */
    public static void scan(ArrayList<File> list, String path, final String prefix, final String suffix, int limt) {
        // 当超出文件数量限制，则退出扫描，结束递归
        if (list.size() >= limt) {
            return;
        }

        File dir = new File(path);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (list.size() >= limt) {
                    return;
                }
                if (file.isDirectory()) {
                    scan(list, file.getAbsolutePath(), prefix, suffix, limt);
                } else if (file.getName().toLowerCase().startsWith(prefix.toLowerCase()) && file.getName().toLowerCase().endsWith(suffix.toLowerCase())) {
                    list.add(file);
                }
            }
        }
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param dirPath 将要删除的文件目录
     */
    public static boolean deleteDir(String dirPath) {
        File dir = new File(dirPath);
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(dir + File.separator + children[i]);
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * 获取文件大小
     * @param filePath
     * @return
     */
    public static long getFileSize(String filePath){
        File file = new File(filePath);
        if (file.exists() && file.isFile()){
            return file.length();
        }else{
            return 0;
        }
    }

    /**
     * 按行合并文件
     * @param combinePath
     * @param prefix
     * @param suffix
     * @param outFilePath
     */
    public static int lineCombineFile(String combinePath, String prefix, String suffix, String outFilePath) {
        return lineCombineFile(null, combinePath, prefix, suffix, outFilePath, "UTF-8");
    }

    /**
     * 按行合并文件
     * @param header
     * @param combinePath
     * @param prefix
     * @param suffix
     * @param outFilePath
     */
    public static int lineCombineFile(String header, String combinePath, String prefix, String suffix, String outFilePath) {
        return lineCombineFile(header, combinePath, prefix, suffix, outFilePath, "UTF-8");
    }

    /**
     * 按行合并文件
     * @param header
     * @param combinePath
     * @param prefix
     * @param suffix
     * @param outFilePath
     * @param encoding
     */
    public static int lineCombineFile(String header, String combinePath, String prefix, String suffix, String outFilePath, String encoding) {
        int count = 0;

        BufferedWriter bufWriter = null;
        try {
            bufWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFilePath), encoding));

            if(header != null) {
                bufWriter.write(header);
                bufWriter.write("\r\n");
            }

            File[] files = Utils.scan(combinePath, prefix, suffix);
            BufferedReader bufReader = null;
            for (int i = 0; i < files.length; i++) {
                try {
                    bufReader = new BufferedReader(new FileReader(files[i]));
                    String line = null;
                    while ((line = bufReader.readLine()) != null) {
                        bufWriter.write(line);
                        bufWriter.write("\r\n");
                        count++;

                        if(count % 100000 ==0){
                            //每10W行刷新缓冲区的数据到文件
                            bufWriter.flush();
                        }
                    }
                } catch (Exception e) {
                    e.getStackTrace();
                } finally {
                    if (bufReader != null) {
                        try {
                            bufReader.close();
                        } catch (IOException e) {
                            e.getStackTrace();
                        }
                    }
                }
            }
            bufWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufWriter != null) {
                try {
                    bufWriter.close();
                } catch (IOException e) {
                    e.getStackTrace();
                }
            }
        }
        return count;
    }

    /**
     * 使用Java.nio ByteBuffer合并文件
     * @param header
     * @param combinePath
     * @param prefix
     * @param suffix
     * @param outFilePath
     */
    public static void nioCombineFile(String header, String combinePath, String prefix, String suffix, String outFilePath) {
        FileOutputStream out = null;
        FileChannel fcOut = null;
        try {
            // 换行符
            ByteBuffer lineSepbb = ByteBuffer.wrap(System.getProperty("line.separator").getBytes());
            // 每次读写1M
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);

            // 获取源文件和目标文件的输入输出流
            out = new FileOutputStream(outFilePath);
            fcOut = out.getChannel();

            if(header != null) {
                // 写入文件头部
                fcOut.write(ByteBuffer.wrap(header.getBytes()));
                fcOut.write(lineSepbb);
            }

            File[] files = Utils.scan(combinePath, prefix, suffix);
            FileInputStream in = null;
            FileChannel fcIn = null;
            for (int i = 0; i < files.length; i++) {
                try {
                    in = new FileInputStream(files[i]);
                    // 获取输入输出通道
                    fcIn = in.getChannel();
                    while (true) {
                        // clear方法重设缓冲区，使它可以接受读入的数据
                        buffer.clear();
                        // 从输入通道中将数据读到缓冲区
                        int r = fcIn.read(buffer);
                        if (r == -1) {
                            break;
                        }
                        // flip方法让缓冲区可以将新读入的数据写入另一个通道
                        buffer.flip();
                        fcOut.write(buffer);
                    }
                    fcOut.write(lineSepbb);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if(in != null){
                            in.close();
                        }
                        if(fcIn != null) {
                            fcIn.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(out != null){
                    out.close();
                }
                if(fcOut != null) {
                    fcOut.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 将127.0.0.1形式的IP地址转换成十进制整数，这里没有进行任何错误处理
    public static long ipToLong(String strIp) {
        long[] ip = new long[4];
        // 先找到IP地址字符串中.的位置
        int position1 = strIp.indexOf(".");
        int position2 = strIp.indexOf(".", position1 + 1);
        int position3 = strIp.indexOf(".", position2 + 1);
        // 将每个.之间的字符串转换成整型
        ip[0] = Long.parseLong(strIp.substring(0, position1));
        ip[1] = Long.parseLong(strIp.substring(position1 + 1, position2));
        ip[2] = Long.parseLong(strIp.substring(position2 + 1, position3));
        ip[3] = Long.parseLong(strIp.substring(position3 + 1));
        return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
    }

    // 将十进制整数形式转换成127.0.0.1形式的ip地址
    public static String longToIP(long longIp) {
        StringBuffer sb = new StringBuffer("");

        if (longIp < 0) {
            longIp = Long.parseLong(String.valueOf(((longIp & 0xFFFFFFFF) << 32 >>> 32)));
        }
        // 直接右移24位
        sb.append(String.valueOf((longIp >>> 24)));
        sb.append(".");
        // 将高8位置0，然后右移16位
        sb.append(String.valueOf((longIp & 0x00FFFFFF) >>> 16));
        sb.append(".");
        // 将高16位置0，然后右移8位
        sb.append(String.valueOf((longIp & 0x0000FFFF) >>> 8));
        sb.append(".");
        // 将高24位置0
        sb.append(String.valueOf((longIp & 0x000000FF)));
        return sb.toString();
    }

    /**
     * 十六进制字符串转换为IPV4
     *
     * @param hex
     * @return
     */
    public static String hex2Ip(String hex) {
        StringBuffer sb = new StringBuffer("");

        if (hex != null) {
            hex = hex.trim();

            if (hex.length() == 8) {

                sb.append(Integer.parseInt(hex.substring(0, 1), 16));
                sb.append(".");
                sb.append(Integer.parseInt(hex.substring(2, 3), 16));
                sb.append(".");
                sb.append(Integer.parseInt(hex.substring(4, 5), 16));
                sb.append(".");
                sb.append(Integer.parseInt(hex.substring(6, 7), 16));
                sb.append(".");
            }
        }

        return sb.toString();
    }

    public static void main(String[] args) {

        long s = System.currentTimeMillis();
        ArrayList<File> files = new ArrayList<File>();
        scan(files, "d:/", "", ".java", 10);

        for (File f : files) {
            if (f != null) {
                System.out.println(f.getAbsolutePath());
            }
        }

        System.out.println(System.currentTimeMillis() - s);

    }

}
