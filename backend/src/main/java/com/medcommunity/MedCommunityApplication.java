package com.medcommunity;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.medcommunity.mapper")
public class MedCommunityApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedCommunityApplication.class, args);
    }
}
