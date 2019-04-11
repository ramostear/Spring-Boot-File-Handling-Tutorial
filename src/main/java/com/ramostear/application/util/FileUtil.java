package com.ramostear.application.util;

import java.io.File;

public class FileUtil {
    public static void createDirectories(String pathname) {
        File directories = new File(pathname);
        if (directories.exists()) {
            System.out.println("文件上传根目录已存在");
        } else { // 如果目录不存在就创建目录
            if (directories.mkdirs()) {
                System.out.println("创建多级目录成功");
            } else {
                System.out.println("创建多级目录失败");
            }
        }
    }
}
