package com.ezjcc.picops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PicOpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PicOpsApplication.class, args);
    }
}
