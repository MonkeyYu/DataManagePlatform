package com.etone.universe.dmp.util;

import com.etone.daemon.util.XFiles;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Lanny on 2016-8-30.
 */
public class FileOperatorTest {

    @Test
    public void testCut() throws Exception {
        String source = "d:/temp/ftbAppSession_20141224_1419502886704_pool-1-thread-2_20141224.done.bak";
        String target = "e:/temp/ftbAppSession_20141224";
        long size = 1024*1024*100;
        String sourceCode = "gbk";
        String targetCode = "gbk";
        ArrayList list = XFiles.cut(source, target, size, sourceCode, targetCode, false);
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
        System.out.println("finish: " + list.size());
    }

    @Test
    public void testMerge() throws Exception {

//        FileOperator.merge("e:/temp/data", "e:/temp/data.utf-8", "utf-8", "gbk", "", "");
//        FileOperator.merge("e:/temp/data", "e:/temp/data.iso", "ISO8859_1", "gbk", "", "");
//        FileOperator.merge("e:/temp/data", "e:/temp/data.gbk", "gbk", "gbk", "", "");

        File file = new File("d:/temp");
        System.out.println(file.isDirectory());


    }
}
