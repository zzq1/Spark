package com.neu.zzq.homework.test;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Created by zzq on 2018/1/15.
 */
public class test {
    public static List<String>[] getList(){
        List<String>[] outputs = null;
        try {
            String dataDir = "C:\\Users\\zzq\\Desktop\\Storm\\code\\Storm-Case-02\\logs\\";
            File file = new File(dataDir);
            Collection<File> listFiles = FileUtils.listFiles(file, new String[]{"log"},true);

            for (File f : listFiles) {
                List<String> readLines = FileUtils.readLines(f);
                System.out.print(readLines.size());
                for (int i = 0;i < readLines.size(); i++){
                    //for (String line : readLines) {
                        outputs[i] = readLines;
//          this._collector.emit(new Values(line.trim()));
                   // }
                }

                try {
                    File srcFile = f.getAbsoluteFile();
                    File destFile = new File(srcFile + ".done." + System.currentTimeMillis());
                    FileUtils.moveFile(srcFile, destFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputs;
    }
    public static void main(String[] args){
        test t = new test();
        t.getList();
    }
}
