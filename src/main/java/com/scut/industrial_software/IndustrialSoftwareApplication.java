package com.scut.industrial_software;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ServletComponentScan
@EnableScheduling          // 开启定时任务支持
@MapperScan("com.scut.industrial_software.mapper")
public class IndustrialSoftwareApplication {

    public static void main(String[] args) {
        SpringApplication.run(IndustrialSoftwareApplication.class, args);
    }

}
