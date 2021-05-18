package com.jd.product.init;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * @Author chenYongJin
 * @Date 2021-05-18
 */
@Component
public class Init implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        //初始化配置文件
        String dir = System.getProperty("user.dir")+"/config";
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        //创建文件
        file = new File(dir + "/config.txt");
        if (!file.exists()) {
            file.createNewFile();
            System.out.println("创建配置：config.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write("E:/jd_download");
            writer.flush();
            writer.close();
        }
        file = new File(dir + "/log.txt");
        if (!file.exists()) {
            file.createNewFile();
            System.out.println("创建配置：log.txt");
        }
        file = new File(dir + "/cookie.txt");
        if (!file.exists()) {
            file.createNewFile();
            System.out.println("创建配置：cookie.txt");
        }
    }
}
