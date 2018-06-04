package com.etone.universe.dmp.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.etone.daemon.util.Mixeds;
import com.etone.daemon.util.XFiles;

/**
 * Created by Lanny on 2016-8-29.
 */
public class FileOperator {
    private static final Logger logger = LoggerFactory.getLogger(FileOperator.class);

    /**
     * 将目录下的文件合并为一个大文件
     *
     * @param path
     * @param target
     * @param sourceCode
     * @param targetCode
     * @param prefix
     * @param suffix
     * @param delSource
     */
    public static void merge(String path, String target, String sourceCode, String targetCode, String prefix, String suffix, boolean delSource) throws IOException {
        merge(path, target, sourceCode, targetCode, prefix, suffix, delSource, null);
    }

    /**
     * 将目录下的文件合并为一个大文件
     *
     * @param path
     * @param target
     * @param sourceCode
     * @param targetCode
     * @param prefix
     * @param suffix
     * @param head
     * @param delSource
     * @return 被合并的文件个数
     */
    public static int merge(String path, String target, String sourceCode, String targetCode, String prefix, String suffix, boolean delSource, String head) throws IOException {

        File dir = new File(path);
        if (!Mixeds.isNotNull(dir)) {
            logger.error("file not exists : ", path);
        }

        //遍历文件加下的文件，合并文件
        logger.info("start merge dir {}", path);
        File[] files = XFiles.scan(path, prefix, suffix);
        if (files == null || files.length == 0) {
            logger.info("no file to merge in path : {}", path);
            return 0;
        }

        File targetFile = new File(target + ".tmp");
        BufferedWriter bWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), targetCode), 102400);
        if (head != null && !"".equals(head)) {
            // 写入文件头部
            bWriter.append(head);
            bWriter.newLine();
        }

        for (File file : files) {
            BufferedReader bReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), sourceCode));
            String line = null;
            long count = 0;

            while ((line = bReader.readLine()) != null) {
                // 写入行数据
                bWriter.append(line);
                bWriter.newLine();
                count = count + line.getBytes().length + 2;
            }

            bReader.close();

            // delete source file
            if (delSource) {
                file.delete();
            }
        }

        bWriter.flush();
        bWriter.close();

        targetFile.renameTo(new File(target));
        return files.length;
    }

    /**
     * 修改文件名后缀
     *
     * @param file
     * @param newSuffix
     */
    public static String modifySuffix(File file, String newSuffix) {
        String targetFile = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(".")) + "." + newSuffix;
        file.renameTo(new File(targetFile));
        logger.debug("rename file : {} to {}", file.getAbsolutePath(), targetFile);
        return targetFile;
    }


}

