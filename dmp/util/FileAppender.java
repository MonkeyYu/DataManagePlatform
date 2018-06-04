package com.etone.universe.dmp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Lanny on 2016-4-6.
 */
public class FileAppender {

    private static final Logger logger = LoggerFactory.getLogger(FileAppender.class);

    private ConcurrentHashMap<String, AFile> openList = new ConcurrentHashMap<String, AFile>();

    private String encode = "utf-8";

    private long size = 1024 * 100;

    private int second = 500;

    private String suffix = ".txt";

    private String path = "";

    /**
     * 构造函数
     */
    public FileAppender(String path, String encode, long size, final int second) {

        this.path = path;
        this.encode = encode;
        this.size = size;
        this.second = second;

        // 启动检查线程，关闭超时的文件
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    logger.info("check and check timeout files");
                    check();
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 根据KEY定向到具体的子文件，并往文件中追加记录行
     *
     * @param key
     * @param line
     */
    public void append(String key, String line) {
        // 线程安全
        AFile file = openList.get(key);

        // 如果文件不存在，则创建文件并保存在已打开文件列表中
        if (file == null) {
            file = new AFile(key);
            openList.put(key, file);
            logger.info("create file : {}", file.fileName);
        }

        file.append(line);

        // 如果文件大小超出阀值，则关闭文件
        if (file.size >= this.size) {
            openList.remove(file.key);
            file.close();
            logger.info("file size {} > config.size {} & close file : {}", file.size, this.size, file.fileName);
        }
    }

    /**
     * 检查已打开文件列表中是否存在超时的文件
     */
    public void check() {
        Set<Map.Entry<String, AFile>> set = openList.entrySet();
        ArrayList<AFile> closeList = new ArrayList<AFile>();

        Iterator<Map.Entry<String, AFile>> iterator = set.iterator();
        while (iterator.hasNext()) {
            AFile file = iterator.next().getValue();

            // 如果文件已经超出分割大小或者打开时间
            if ((System.currentTimeMillis() - file.time) >= this.second * 1000) {
                closeList.add(file);
            }
        }

        for (AFile file : closeList) {
            openList.remove(file.key);
            file.close();
            logger.info("timeout and close file : {}", file.fileName);
        }
    }


    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    private class AFile {

        // 文件创建的时间，取被创建时的时间戳
        private long time = System.currentTimeMillis();

        // 文件大小
        private long size = 0;

        // 写入Writer
        private BufferedWriter bWriter = null;

        // 文件名称
        private String fileName = "appender-default.txt";

        // key
        private String key = "default";

        /**
         * 构造函数，根据key生成文件并，创建文件写入流
         *
         * @param key
         */
        public AFile(String key) {

            this.key = key;

            try {
                fileName = key + "_" + System.currentTimeMillis() + ".tmp";
                File parent = new File(path);
                fileName = parent.getAbsolutePath() + File.separatorChar + fileName;
                bWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), encode));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 关闭文件和文件输入流，并重命名文件
         */
        public void close() {

            try {
                if (bWriter != null) {
                    bWriter.close();
                    File file = new File(fileName);
                    file.renameTo(new File(file.getAbsolutePath().replaceAll(".tmp", getSuffix())));
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        /**
         * 往文件中追加记录行
         *
         * @param line
         */
        public void append(String line) {

            try {
                bWriter.append(line);
                bWriter.newLine();
                bWriter.flush();
                // 累计文件大小
                size = size + line.getBytes().length;
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

}
